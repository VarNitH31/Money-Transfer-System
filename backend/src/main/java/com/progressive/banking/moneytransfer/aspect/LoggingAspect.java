package com.progressive.banking.moneytransfer.aspect;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final String CORRELATION_ID = "correlationId";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    // Define sensitive field names that should be masked
    private static final List<String> SENSITIVE_KEYS =
            List.of("password", "secret", "token", "authorization", "idempotencyKey", "otp", "pin");

    /**
     * Controllers: adjust base package to your controller package if different
     */
    @Pointcut("within(com.progressive.banking.moneytransfer..controller..*)")
    public void controllerLayer() {}

    /**
     * Services: adjust base package to your service package if different
     */
    @Pointcut("within(com.progressive.banking.moneytransfer..service..*)")
    public void serviceLayer() {}

    /**
     * Log controller + service calls
     */
    @Around("controllerLayer() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        ensureCorrelationId();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String signature = className + "." + methodName + "()";

        String requestSummary = getRequestSummary();
        String args = formatArgs(joinPoint.getArgs());

        StopWatch sw = new StopWatch();
        sw.start();

        // ENTRY LOG
        if (requestSummary != null) {
            log.info("[{}] --> {} | {}", MDC.get(CORRELATION_ID), signature, requestSummary);
        } else {
            log.info("[{}] --> {}", MDC.get(CORRELATION_ID), signature);
        }

        if (!args.isBlank()) {
            log.debug("[{}]     args={}", MDC.get(CORRELATION_ID), args);
        }

        try {
            Object result = joinPoint.proceed();
            sw.stop();

            // EXIT LOG
            log.info("[{}] <-- {} | timeMs={}", MDC.get(CORRELATION_ID), signature, sw.getTotalTimeMillis());

            // Response logging (keep it compact)
            String responseSummary = summarizeResponse(result);
            if (!responseSummary.isBlank()) {
                log.debug("[{}]     response={}", MDC.get(CORRELATION_ID), responseSummary);
            }

            return result;

        } catch (Throwable ex) {
            sw.stop();
            log.error("[{}] xx  {} | timeMs={} | ex={} : {}",
                    MDC.get(CORRELATION_ID),
                    signature,
                    sw.getTotalTimeMillis(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex
            );
            throw ex;
        }
    }

    /**
     * Ensures a correlation id exists in MDC.
     * If request has X-Correlation-Id, use it; else generate one.
     */
    private void ensureCorrelationId() {
        if (MDC.get(CORRELATION_ID) != null) return;

        HttpServletRequest req = getCurrentRequest();
        String incoming = (req != null) ? req.getHeader(CORRELATION_HEADER) : null;

        String cid = (incoming != null && !incoming.isBlank()) ? incoming : UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, cid);
    }

    /**
     * Extracts minimal request info for controller logs.
     */
    private String getRequestSummary() {
        HttpServletRequest req = getCurrentRequest();
        if (req == null) return null;

        String method = req.getMethod();
        String uri = req.getRequestURI();
        String query = req.getQueryString();

        return (query == null) ? (method + " " + uri) : (method + " " + uri + "?" + query);
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return (attrs != null) ? attrs.getRequest() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Formats and masks arguments.
     * - Skips servlet/request/response objects
     * - Masks sensitive values
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";

        return Arrays.stream(args)
                .filter(arg -> arg != null)
                .filter(arg -> !isInfrastructureArg(arg))
                .map(this::safeToStringMasked)
                .collect(Collectors.joining(", "));
    }

    private boolean isInfrastructureArg(Object arg) {
        String name = arg.getClass().getName();
        return name.startsWith("jakarta.servlet.")
                || name.startsWith("javax.servlet.")
                || name.startsWith("org.springframework.validation")
                || name.startsWith("org.springframework.web")
                || name.startsWith("org.springframework.http")
                || name.contains("HttpServletRequest")
                || name.contains("HttpServletResponse");
    }

    /**
     * Convert object to string safely and mask sensitive keys.
     * For DTOs, Lombok @Data toString prints fields -> we mask by keywords.
     */
    private String safeToStringMasked(Object arg) {
        String raw;
        try {
            raw = String.valueOf(arg);
        } catch (Exception e) {
            return "<unprintable>";
        }
        return maskSensitive(raw);
    }

    /**
     * Very simple keyword-based masking (works well with DTO toString()).
     * Example: "idempotencyKey=abcd" -> "idempotencyKey=****"
     */
    private String maskSensitive(String input) {
        String masked = input;
        for (String key : SENSITIVE_KEYS) {
            // handle patterns like key=VALUE or key: VALUE
            masked = masked.replaceAll("(?i)(" + key + "\\s*[=:]\\s*)([^,}\\]]+)", "$1****");
        }
        return masked;
    }

    /**
     * Keeps response logging compact and avoids huge payloads.
     */
    private String summarizeResponse(Object result) {
        if (result == null) return "";

        try {
            if (result instanceof ResponseEntity<?> re) {
                Object body = re.getBody();
                int status = re.getStatusCode().value();
                String bodyStr = (body == null) ? "null" : maskSensitive(body.toString());
                bodyStr = trimIfTooLong(bodyStr, 700);
                return "status=" + status + ", body=" + bodyStr;
            }

            String out = maskSensitive(result.toString());
            return trimIfTooLong(out, 700);

        } catch (Exception e) {
            return "<unprintable-response>";
        }
    }

    private String trimIfTooLong(String value, int max) {
        if (value == null) return "";
        if (value.length() <= max) return value;
        return value.substring(0, max) + "...(truncated)";
    }
}