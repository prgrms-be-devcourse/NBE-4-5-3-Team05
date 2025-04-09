package com.NBE_4_5_2.Team5.domain.notification.service

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification
import com.NBE_4_5_2.Team5.domain.notification.repository.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationService(private val notificationRepository: NotificationRepository) {
    fun getNotificationsAfter(lastId: Long?): List<Notification?>? {
        return notificationRepository.findAllBy_idAfter(lastId)
    }
}
