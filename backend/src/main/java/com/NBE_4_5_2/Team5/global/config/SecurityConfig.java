package com.NBE_4_5_2.Team5.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // ✅ 모든 요청 인증 없이 허용
                )
                .csrf(csrf -> csrf.disable())  // ✅ CSRF 보호 비활성화 (POST, PUT, DELETE 가능)
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // ✅ H2 Console iframe 허용

        return http.build();
    }
}
