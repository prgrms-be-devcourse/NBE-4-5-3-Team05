package com.NBE_4_5_2.Team5.domain.notification.repository

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification?, String?> {
    fun findAllBy_idAfter(idAfter: Long?): List<Notification?>?
}
