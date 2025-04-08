package com.NBE_4_5_2.Team5.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification;

@Service
public class KafkaNotificationProducerService {
	private final KafkaTemplate<Long, Notification> kafkaTemplate;

	public KafkaNotificationProducerService(KafkaTemplate<Long, Notification> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendMessage(Notification message) {
		kafkaTemplate.send("sse-topic",message.getId(), message);
	}
}
