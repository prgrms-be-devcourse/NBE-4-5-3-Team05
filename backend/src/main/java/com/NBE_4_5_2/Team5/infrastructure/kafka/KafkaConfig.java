package com.NBE_4_5_2.Team5.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
	@Bean
	public NewTopic sseTopic() {
		return new NewTopic("sse-topic", 1, (short) 1);
	}
}
