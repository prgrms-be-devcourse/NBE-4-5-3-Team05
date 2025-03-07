package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import util.Util;

@TestConfiguration
public class TestConfig {
	@Bean
	public Util util() {
		return new Util();
	}
}
