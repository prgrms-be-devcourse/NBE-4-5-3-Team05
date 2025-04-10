package com.NBE_4_5_2.Team5.infrastructure.kafka

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification
import com.NBE_4_5_2.Team5.domain.notification.repository.NotificationRepository
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.standard.util.Ut
import com.NBE_4_5_2.Team5.global.standard.util.Ut.Notification.parse
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Component
class KafkaConsumer(
    private var userService: UserService,
    private var notificationRepository: NotificationRepository
) {
    // SseEmitter를 저장할 스레드 안전 컬렉션
    private val emitters: MutableMap<String, SseEmitter> = ConcurrentHashMap()
    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)


    private val loggedInUser: User
        get() = userService.userIdentity

    // 클라이언트가 SSE 연결 시에 emitter를 등록
    fun addEmitter(emitter: SseEmitter): SseEmitter {
        val userId = loggedInUser.id

        if (!emitters.containsKey(userId)) {
            emitters[userId] = emitter
        }

        // 클라이언트가 수동으로 연결 끊거나 타임아웃 발생 시 리스트에서 제거
        emitter.onCompletion { emitters.remove(userId) }
        emitter.onTimeout { emitters.remove(userId) }

        return emitter
    }

    fun ping() {
            synchronized(emitters) {
                val iterator = emitters.entries
                    .stream().map { entry: Map.Entry<String, SseEmitter> -> entry }.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    val emitter = next.value
                    val key = next.key

                        logger.info("ping 전송중")
                        emitter.send(
                            SseEmitter.event()
                                .comment("ping")
                                .data("ping")
                        )


                }

            }

    }

    @KafkaListener(topics = ["sse-topic"], groupId = "sse-demo-group")
    fun consume(record: ConsumerRecord<String, Notification>) {
        val id = record.key()
        val notification = record.value()

        // 새 메시지를 수신하면, 모든 SseEmitter에 알림
        synchronized(emitters) {
            val iterator = emitters.entries
                .stream().map { entry: Map.Entry<String, SseEmitter> -> entry.value }.iterator()
            if (notification.global) {
                while (iterator.hasNext()) {
                    val emitter = iterator.next()
                    try {
                        emitter.send(
                            SseEmitter.event()
                                .name("kafka-event") // 커스텀 이벤트명
                                .data(notification.content?:"")
                        )
                    } catch (e: Exception) {
                        // 전송 실패 시 emitter 제거
                        iterator.remove()
                    }
                }
            } else {
                try {
                    val sseEmitter = emitters[notification.userId]
                    sseEmitter?.send(
                        SseEmitter.event()
                            .name("kafka-event") // 커스텀 이벤트명
                            .data(notification.content?:"")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 전송 실패 시 emitter 제거
                    iterator.remove()
                }
            }
        }
    }

    fun removeEmitter(userId: String) {
        emitters.remove(userId);
    }
}
