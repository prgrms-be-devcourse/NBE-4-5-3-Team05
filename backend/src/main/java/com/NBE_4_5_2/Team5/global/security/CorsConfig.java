package com.NBE_4_5_2.Team5.global.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

@Configuration
public class CorsConfig implements CorsConfigurationSource {
	@Value("${custom.front.host}")
	private String frontHost;

	private String ALLOWED_ORIGIN;
	private static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

	@Override
	public CorsConfiguration getCorsConfiguration(@NonNull HttpServletRequest request) {
		CorsConfiguration config = new CorsConfiguration();
		ALLOWED_ORIGIN = "https://%s".formatted(frontHost);
		config.setAllowedOrigins(List.of(ALLOWED_ORIGIN, "http://localhost:3000"));
		config.setAllowedMethods(ALLOWED_METHODS);
		config.setAllowCredentials(true);
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
		config.setExposedHeaders(List.of("Authorization"));
		config.setMaxAge(3600L);

		return config;
	}
}