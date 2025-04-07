package com.NBE_4_5_2.Team5

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class Team5Application

fun main(args: Array<String>) {
    val app = org.springframework.boot.SpringApplication(Team5Application::class.java)
    app.addListeners(ApplicationPidFileWriter())
    app.run(*args)
}
