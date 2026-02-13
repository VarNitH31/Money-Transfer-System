package com.progressive.banking.moneytransfer.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);

                String username = jwtUtil.extractUsername(token);

                if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                    var userDetails =
                            userDetailsService.loadUserByUsername(username);

                    // ⚠️ IMPORTANT: do not fail hard if validate() returns false
                    if (jwtUtil.validate(token)) {
                    	System.out.println("TOKEN RECEIVED: {}" + token);
                    	System.out.println("VALID = {}" + jwtUtil.validate(token));
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());

                        auth.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        log.warn("JWT validation returned false");
                    }
                }

            } catch (Exception ex) {
                log.error("JWT parsing error: {}", ex.getMessage());
                // DO NOT write response here
            }
        }

        chain.doFilter(request, response);
    }
}