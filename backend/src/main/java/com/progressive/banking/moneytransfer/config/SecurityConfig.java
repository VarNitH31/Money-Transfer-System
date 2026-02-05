package com.progressive.banking.moneytransfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // For REST APIs / Postman testing
            .csrf(csrf -> csrf.disable())

            // Allow everything without authentication
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // Disable default login mechanisms
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}