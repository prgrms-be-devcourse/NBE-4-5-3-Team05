package com.NBE_4_5_2.Team5.domain.notification.controller

import com.NBE_4_5_2.Team5.domain.notification.service.NotificationService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.infrastructure.kafka.KafkaConsumer
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@RestController
class NotificationController(
    private val kafkaConsumer: KafkaConsumer, private val notificationService: NotificationService,
    private val userService: UserService
) {
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = ["/api/notification/subscribe"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(@RequestHeader(value = "Last-Event-ID", required = false) lastEventId: String?): SseEmitter {
        val emitter = SseEmitter(0L)
        kafkaConsumer.addEmitter(emitter)


        if (lastEventId != null) {
            val lastId = lastEventId.toLong()
            val missed = notificationService.getNotificationsAfter(lastId)

            for (n in missed!!) {
                try {
                    if (n!!.global || (!n.global && n.userId == userService.userIdentity.id)) {
                        emitter.send(
                            SseEmitter.event()
                                .id(n.id.toString())
                                .data(n.content?:"")
                        )
                    }
                } catch (e: IOException) {
                    kafkaConsumer.removeEmitter(n!!.userId)
                    break
                }
            }
        }

        return emitter
    }
}
