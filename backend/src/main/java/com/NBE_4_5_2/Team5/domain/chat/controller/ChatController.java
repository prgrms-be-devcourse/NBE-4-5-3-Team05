package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.dto.ChatRoomDto;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name = "Chat Room API", description = "채팅방 관리 API")
public class ChatController {

	private final ChatRoomService chatRoomService;
	private final ChatService chatService;
	private final AuthTokenService authTokenService;
	private final SimpMessagingTemplate simpMessagingTemplate;

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
		// 업데이트
		updateRooms(token);
	}

	@Operation(summary = "채팅방 업데이트", description = "WebSocket을 통해 업데이트 된 채팅방을 서버로 전송")
	@MessageMapping("/chat/rooms")
	public void updateRooms(@Header("token") String token) {
		String nickname = authTokenService.getUsernameFromToken(token);
		// 채팅방 목록 업데이트 후 클라이언트에게 전송
		List<ChatRoom> updatedRooms = chatRoomService.findRoomByUser(nickname); // 업데이트된 채팅방 목록 조회

		List<ChatRoomDto> dtos = updatedRooms.stream()
						.map(chatRoom -> {
							List<ChatMessage> messages = chatRoomService.getMessagesByUser(chatRoom.getRoomId(), nickname);
							// 초기값
							ChatMessage.MessageType messageType = ChatMessage.MessageType.TALK;
							// 상대방
							String other = chatRoomService.findOther(chatRoom.getRoomId(),nickname);

							if(!messages.isEmpty()) {
								messageType = messages.get(messages.size() - 1).getType();
							}
							System.out.println("lastMessage!!!!!: " + chatRoom.getLastMessage());
							System.out.println("lastTimestamp!!!!!: " + chatRoom.getLastTimestamp());
							return new ChatRoomDto(
									chatRoom.getId(),
									chatRoom.getRoomId(),
									chatRoom.getName(),
									chatRoom.getUserCount(),
									chatRoom.getLastMessage(),
									messageType,
									chatRoom.getLastTimestamp(),
									other
							);
						})
				.toList();
		System.out.println("===DTO===");
		System.out.println(dtos.get(dtos.size()-1).getLastMessage());
		System.out.println(dtos.get(dtos.size()-1).getLastTimestamp());
		System.out.println(dtos.get(dtos.size()-1).getMessageType());
		simpMessagingTemplate.convertAndSend("/sub/chat/rooms", dtos); // 클라이언트에게 전송
	}

}