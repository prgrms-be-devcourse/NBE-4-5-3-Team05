package com.NBE_4_5_2.Team5.global.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class RedisSubscriber(
    private val objectMapper: ObjectMapper,
    private val messagingTemplate: SimpMessageSendingOperations
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 Redis Subscriber가 해당 메시지를 받아 처리한다.
     */
    fun sendMessage(publishMessage: String) {
        try {
            // ChatMessage 객체로 맵핑
            val chatMessage = objectMapper.readValue(publishMessage, ChatMessage::class.java)
            // 채팅방을 구독한 클라이언트에게 메시지 발송
            messagingTemplate.convertAndSend(
                "/sub/chat/room/${chatMessage.roomId}",
                chatMessage
            )
        } catch (e: Exception) {
            log.error("Redis 메시지 처리 중 예외 발생", e)
        }
    }
}
