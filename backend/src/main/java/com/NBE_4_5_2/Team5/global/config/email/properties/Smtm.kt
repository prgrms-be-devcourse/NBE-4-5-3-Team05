package com.NBE_4_5_2.Team5.global.config.email.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * SMTP 프로퍼티 설정을 위한 클래스입니다.
 * application.yml 파일에 정의된 spring.mail.properties.mail.smtp 접두어 설정 값들이 주입됩니다.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.mail.properties.mail.smtp")
data class Smtm(
    var auth: Boolean = false,
    var starttlsEnable: Boolean = false,
    var starttlsRequired: Boolean = false,
    var connectionTimeout: Int = 0,
    var timeout: Int = 0,
    var writeTimeout: Int = 0
)