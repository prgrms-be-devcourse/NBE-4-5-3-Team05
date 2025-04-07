package com.NBE_4_5_2.Team5.global.app

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.context.request.RequestContextListener
import org.springframework.web.filter.RequestContextFilter

@Configuration
class AppConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun requestContextListener(): RequestContextListener = RequestContextListener()

    @Bean
    fun requestContextFilter(): FilterRegistrationBean<RequestContextFilter> =
        FilterRegistrationBean<RequestContextFilter>().apply {
            filter = RequestContextFilter()
            order = Int.MIN_VALUE
        }
}
