package com.NBE_4_5_2.Team5.global.config.email

import com.NBE_4_5_2.Team5.global.config.email.properties.Mail
import com.NBE_4_5_2.Team5.global.config.email.properties.Pop3
import com.NBE_4_5_2.Team5.global.config.email.properties.Smtm
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*

@Configuration
@EnableConfigurationProperties(Pop3::class, Mail::class, Smtm::class)
class EmailConfig(
    private val pop3: Pop3,
    private val mail: Mail,
    private val smtm: Smtm,
) {

    @Bean
    fun javaMailSender(): JavaMailSender = JavaMailSenderImpl().apply {
        host = mail.host
        port = mail.port
        username = mail.username
        password = mail.password
        defaultEncoding = "UTF-8"
        javaMailProperties = mailProperties()
    }

    fun mailProperties(): Properties =
        Properties().apply {
            put("mail.smtp.auth", smtm.auth)
            put("mail.smtp.starttls.enable", smtm.starttlsEnable)
            put("mail.smtp.starttls.required", smtm.starttlsRequired)
            put("mail.smtp.connectiontimeout", smtm.connectionTimeout)
            put("mail.smtp.timeout", smtm.timeout)
            put("mail.smtp.writetimeout", smtm.writeTimeout)
        }

    @Bean
    fun pop3MailProperties(): Properties =
        Properties().apply {
            put("mail.pop3.host", pop3.host)
            put("mail.pop3.port", pop3.port.toString())
            put("mail.pop3.protocol", pop3.protocol)
            put("mail.pop3.folder", pop3.folder)
            put("mail.pop3.username", pop3.username)
            put("mail.pop3.password", pop3.password)
            put("mail.pop3.untilTime", pop3.untilTime.toString())
        }

}