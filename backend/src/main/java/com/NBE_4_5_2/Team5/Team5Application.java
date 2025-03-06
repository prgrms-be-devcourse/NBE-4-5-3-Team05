package com.NBE_4_5_2.Team5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class Team5Application {

    public static void main(String[] args) {
        SpringApplication.run(Team5Application.class, args);
    }

}
