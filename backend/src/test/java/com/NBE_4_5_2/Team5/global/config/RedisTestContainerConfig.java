package com.NBE_4_5_2.Team5.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration
public class RedisTestContainerConfig {

    private static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7.0.8-alpine").withExposedPorts(6379);

    static {
        redisContainer.start();
        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString());
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return redisContainer;
    }

    public static void stopContainer() {
        redisContainer.stop();
    }

}
