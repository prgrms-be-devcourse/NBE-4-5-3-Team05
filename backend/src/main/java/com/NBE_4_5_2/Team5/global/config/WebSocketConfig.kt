package com.NBE_4_5_2.Team5.global.config

import com.NBE_4_5_2.Team5.global.handler.StompHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val stompHandler: StompHandler
) : WebSocketMessageBrokerConfigurer {

    @Value("\${custom.front.host}")
    private lateinit var frontHost: String

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.apply {
            enableSimpleBroker("/sub")
            setApplicationDestinationPrefixes("/pub")
        }
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws-stomp")
            .addInterceptors(HttpHandshakeInterceptor())
            .setAllowedOrigins("http://localhost:3000", "https://%s".formatted(frontHost))
            .withSockJS() // sock.js를 통하여 낮은 버전의 브라우저에서도 websocket이 동작할수 있게 합니다.
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(stompHandler)
    }
}