package com.NBE_4_5_2.Team5.global.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@Slf4j  // 로그 출력
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    // 연결된 웹 소켓 세션 리스트
    private List<WebSocketSession> sessions = new ArrayList<>();

    // 웹 소켓이 연결되면
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 세션 추가
        sessions.add(session);
    }

    // 클라이언트가 메세지를 보낼 때 호출
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for (WebSocketSession s : sessions) {
            // 메세지를 보낸 당사자제외, 열린 세션에 메세지 전송
            if (s != session && s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }

    // 연결 해제
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션 제거
        sessions.remove(session);
    }
}
