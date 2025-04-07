package com.NBE_4_5_2.Team5.global.config.email.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * POP3 프로퍼티 설정을 위한 클래스입니다.
 * application.yml 파일에 정의된 pop3 접두어 설정 값들이 주입됩니다.
 */
@Configuration
@ConfigurationProperties(prefix = "pop3")
data class Pop3 (
    var host: String = "",
    var port: Int = 0,
    var protocol: String = "",
    var folder: String = "",
    var username: String = "",
    var password: String = "",
    var untilTime: Int = 0
)
