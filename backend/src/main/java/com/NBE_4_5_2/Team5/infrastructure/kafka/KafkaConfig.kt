package com.NBE_4_5_2.Team5.infrastructure.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig {
    @Bean
    fun sseTopic(): NewTopic {
        return NewTopic("sse-topic", 1, 1.toShort())
    }
}
