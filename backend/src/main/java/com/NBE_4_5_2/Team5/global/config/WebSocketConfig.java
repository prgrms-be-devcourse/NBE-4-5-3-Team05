package com.NBE_4_5_2.Team5.global.config;

import com.NBE_4_5_2.Team5.global.handler.ChatHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //
        registry.addHandler(new ChatHandler(), "/ws/chat")  // 소켓 연결 주소
                // CORS 허용
                .setAllowedOrigins("*");

    }
}
