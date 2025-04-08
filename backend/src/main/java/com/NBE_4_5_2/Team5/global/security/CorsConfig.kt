package com.NBE_4_5_2.Team5.global.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import java.util.List

@Configuration
class CorsConfig(
    @Value("\${custom.front.host}")
    private val frontHost: String
) : CorsConfigurationSource {

    companion object {
        private val ALLOWED_METHODS    = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        private val ALLOWED_HEADERS    = listOf("Authorization", "Content-Type", "Accept", "Origin")
        private val EXPOSED_HEADERS    = listOf("Authorization")
        private const val MAX_AGE: Long = 3600L
    }

    override fun getCorsConfiguration(request: HttpServletRequest): CorsConfiguration =
        CorsConfiguration().apply {
            // 매번 호출 시점에 frontHost 로부터 allowedOrigin 계산
            val allowedOrigin = "https://$frontHost"
            allowedOrigins = listOf(allowedOrigin, "http://localhost:3000")
            allowedMethods = ALLOWED_METHODS
            allowCredentials = true
            allowedHeaders = ALLOWED_HEADERS
            exposedHeaders = EXPOSED_HEADERS
            maxAge = MAX_AGE
        }
}
