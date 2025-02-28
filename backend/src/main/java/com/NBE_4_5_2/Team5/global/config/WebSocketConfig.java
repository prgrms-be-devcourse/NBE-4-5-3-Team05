package com.NBE_4_5_2.Team5.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메세지 구독 경로
        config.enableSimpleBroker("/topic");
        // 메세지 발행 경로 설정
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 채팅
        registry.addEndpoint("/chat")
                .setAllowedOrigins("*")
                .withSockJS();
        // 매칭
        registry.addEndpoint("/match")
                .setAllowedOrigins("*")
                .withSockJS();
    }


}
