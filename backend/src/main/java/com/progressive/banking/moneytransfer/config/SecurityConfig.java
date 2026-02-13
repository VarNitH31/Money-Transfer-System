package com.progressive.banking.moneytransfer.config;

import com.progressive.banking.moneytransfer.security.CustomUserDetailsService;
import com.progressive.banking.moneytransfer.security.JwtAuthenticationFilter;
import com.progressive.banking.moneytransfer.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Bean
    public JwtAuthenticationFilter jwtFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )

            // 🔥 VERY IMPORTANT: ensures errors return JSON instead of blank 403
            .exceptionHandling(ex -> ex
            		.authenticationEntryPoint((request, response, authException) -> {

            		    // 🚨 DO NOT override controller errors
            		    if (response.getStatus() != 200 && response.getStatus() != 0) {
            		        return;
            		    }

            		    response.setStatus(401);
            		    response.setContentType("application/json");
            		    response.getWriter().write("""
            		    {
            		      "errorCode":"AUTH-401",
            		      "message":"Unauthorized - token missing or invalid"
            		    }
            		    """);
            		})
            	    .accessDeniedHandler((request, response, accessDeniedException) -> {

            	        if (response.isCommitted()) {
            	            return;
            	        }

            	        response.setStatus(403);
            	        response.setContentType("application/json");
            	        response.getWriter().write("""
            	        {
            	          "errorCode":"AUTH-403",
            	          "message":"Forbidden - access denied"
            	        }
            	        """);
            	    })
            	)
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
            .formLogin(f -> f.disable())
            .httpBasic(h -> h.disable());

        return http.build();
    }

    // CORS config
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("https://*.devtunnels.ms");
        config.addAllowedOriginPattern("https://*.github.dev");
        config.addAllowedOriginPattern("*");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}