package com.NBE_4_5_2.Team5.global.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public class RedisTestContainerConfig {

	private static final GenericContainer<?> redisContainer;

	static {
		redisContainer =
			new GenericContainer<>("redis:7.0.8-alpine").withExposedPorts(6379);
		redisContainer.start();
	}

	public static GenericContainer<?> redisContainer() {
		return redisContainer;
	}

	public static void stopContainer() {
		redisContainer.stop();
	}

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redisContainer::getHost);
		registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
	}
}
