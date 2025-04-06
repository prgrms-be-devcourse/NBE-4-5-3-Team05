package com.NBE_4_5_2.Team5.global.config.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "pop3")
public class Pop3Properties {
    private String host;
    private int port;
    private String protocol;
    private String folder;
    private String username;
    private String password;
    private int untilTime;

}
