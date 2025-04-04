package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatService;
import com.NBE_4_5_2.Team5.domain.user.user.service.AuthTokenService;
import com.NBE_4_5_2.Team5.global.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name = "Chat Room API", description = "채팅방 관리 API")
public class ChatController {

	private final ChatRoomService chatRoomService;
	private final ChatService chatService;
	private final AuthTokenService authTokenService;
	private final Rq rq;

	/**
	 * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
	 */
	@Operation(summary = "채팅 메시지 전송", description = "WebSocket을 통해 채팅 메시지를 서버로 전송합니다.")
	@MessageMapping("/chat/message")
	public void message(ChatMessage message, @Header("token") String token) {
		String nickname = authTokenService.getUsernameFromToken(token);
		// 로그인 회원 정보로 대화명 설정
		message.setSender(nickname);
		// 채팅방 인원수 세팅
		message.setUserCount(chatRoomService.getUserCount(message.getRoomId()));
		// Websocket에 발행된 메시지를 redis로 발행(publish)
		chatService.sendChatMessage(message);
	}

}