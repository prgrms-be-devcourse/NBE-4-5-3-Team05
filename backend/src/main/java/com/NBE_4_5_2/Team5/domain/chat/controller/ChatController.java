package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatService;
import com.NBE_4_5_2.Team5.domain.user.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;
    private final AuthTokenService authTokenService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("token") String token) {
        String nickname = authTokenService.getUsernameFromToken(token);
        System.out.println("name: " + nickname);

        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);

        // 채팅방 인원수 세팅
        message.setUserCount(chatRoomService.getUserCount(message.getRoomId()));

        // Websocket에 발행된 메시지를 redis로 발행(publish)
        chatService.sendChatMessage(message);
    }

    // 메세지 삭제(개별저장소)
    @DeleteMapping("api/chat/message")
    @ResponseBody
    public void deleteMessage(@RequestParam String roomId, HttpServletRequest request) {
        String token=authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        System.out.println("nickname: " + username);

        chatService.deleteMessageByClient(username,roomId);
    }

}