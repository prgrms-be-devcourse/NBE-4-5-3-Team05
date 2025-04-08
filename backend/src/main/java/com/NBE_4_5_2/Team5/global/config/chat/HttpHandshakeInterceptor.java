package com.NBE_4_5_2.Team5.global.config.chat;


import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;


public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // 쿠키 문자열 가져오기
        String cookieHeader = request.getHeaders().getFirst("Cookie");

        if (cookieHeader != null) {
            // 쿠키 문자열을 파싱하여 attributes에 저장
            String[] cookiesArray = cookieHeader.split("; ");
            for (String cookie : cookiesArray) {
                String[] cookiePair = cookie.split("=");
                if (cookiePair.length == 2) {
                    attributes.put(cookiePair[0], cookiePair[1]); // 쿠키 이름과 값 저장
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}