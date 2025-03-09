package com.NBE_4_5_2.Team5.global.config;

import com.NBE_4_5_2.Team5.domain.user.service.RedisService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
class RedisTestConfig {
    @Bean
    public RedisService redisService() {
        return mock(RedisService.class); // Mockito로 가짜 객체 반환
    }
}

