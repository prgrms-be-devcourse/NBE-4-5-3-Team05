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
                .csrf(csrf -> csrf.disable()) // ✅ CSRF 비활성화
                .cors(cors -> {}) // ✅ CORS 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // ✅ 모든 요청 허용 (임시)
                );

        return http.build();
    }
}
