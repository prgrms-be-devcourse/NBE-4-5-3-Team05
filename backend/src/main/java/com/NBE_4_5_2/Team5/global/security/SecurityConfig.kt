package com.NBE_4_5_2.Team5.global.security

import com.NBE_4_5_2.Team5.global.dto.Empty
import com.NBE_4_5_2.Team5.global.dto.RsData
import com.NBE_4_5_2.Team5.global.standard.util.Ut
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val corsConfig: CorsConfig,
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val customAuthorizationRequestResolver: CustomAuthorizationRequestResolver,
    private val customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
            headers {
                addHeaderWriter(
                    XFrameOptionsHeaderWriter(
                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN
                    )
                )
            }
            csrf {
                disable()
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            cors {
                configurationSource = corsConfig
            }
            oauth2Login {
                authenticationSuccessHandler = customAuthenticationSuccessHandler
                authorizationEndpoint {
                    authorizationRequestResolver = customAuthorizationRequestResolver
                }
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(customAuthenticationFilter)
            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { request, response, authException ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.writer.write(
                        Ut.Json.toString(RsData<Empty>("401-1", "잘못된 인증키입니다."))
                    )
                }

                accessDeniedHandler = AccessDeniedHandler { request, response, accessDeniedException ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.writer.write(
                        Ut.Json.toString(RsData<Empty>("403-1", "접근 권한이 없습니다."))
                    )
                }
            }
        }
        return http.build()
    }
}
