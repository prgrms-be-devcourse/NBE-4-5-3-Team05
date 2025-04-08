package com.NBE_4_5_2.Team5.global.handler

import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService
import com.NBE_4_5_2.Team5.domain.chat.service.ChatService
import com.NBE_4_5_2.Team5.domain.user.user.service.AuthTokenService
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import java.security.Principal

@Component
class StompHandler(
    private val chatRoomService: ChatRoomService,
    private val chatService: ChatService,
    private val authTokenService: AuthTokenService
) : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = StompHeaderAccessor.wrap(message)

        when (accessor.command) {
            StompCommand.CONNECT -> {
                // CONNECT: 처음 연결 시 헤더에서 토큰 꺼내기
                val jwtToken = accessor.getFirstNativeHeader("accessToken")
                log.info("[CONNECT] accessToken={}", jwtToken)
                // authTokenService.getPayload(jwtToken) // 검증 로직
            }

            StompCommand.SUBSCRIBE -> {
                // SUBSCRIBE: destination → roomId, 세션 맵핑, 입장 처리
                val dest = message.headers["simpDestination"] as? String
                    ?: "InvalidRoomId"
                val roomId = chatService.getRoomId(dest)

                val sessionId = message.headers["simpSessionId"] as? String
                chatRoomService.setUserEnterInfo(sessionId, roomId)
                chatRoomService.plusUserCount(roomId)

                val principal = message.headers["simpUser"] as? Principal
                val username = principal?.name ?: "UnknownUser"
                val nickname = authTokenService.getNicknameFromName(username)
                log.info("[SUBSCRIBED] user={}, room={}", nickname, roomId)
            }

            StompCommand.DISCONNECT -> {
                // DISCONNECT: 세션 → roomId, 퇴장 처리
                val sessionId = message.headers["simpSessionId"] as? String
                val roomId = chatRoomService.getUserEnterRoomId(sessionId)
                chatRoomService.minusUserCount(roomId)
                chatRoomService.removeUserEnterInfo(sessionId)
                log.info("[DISCONNECTED] session={}, room={}", sessionId, roomId)
            }

            else -> {
                // 그 외 명령은 무시
            }
        }

        return message
    }
}
