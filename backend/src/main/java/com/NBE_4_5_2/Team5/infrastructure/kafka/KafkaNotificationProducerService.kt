package com.NBE_4_5_2.Team5.infrastructure.kafka

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification
import com.NBE_4_5_2.Team5.domain.notification.repository.NotificationRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaNotificationProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Notification>,
    private val notificationRepository: NotificationRepository
) {
    fun sendMessage(message: Notification) {
        val saved = notificationRepository.save(message);
        kafkaTemplate.send("sse-topic", saved.id.toString(), saved)
    }
}
