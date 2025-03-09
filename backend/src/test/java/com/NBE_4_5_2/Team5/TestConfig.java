package com.NBE_4_5_2.Team5;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

	@Bean
	public Util util() {
		return new Util();
	}
}
