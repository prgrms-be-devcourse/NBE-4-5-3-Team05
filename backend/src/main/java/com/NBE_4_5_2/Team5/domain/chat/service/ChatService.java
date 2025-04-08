package com.NBE_4_5_2.Team5.domain.chat.service;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.repository.ChatMessageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatService {

	private final ChannelTopic channelTopic;
	private final RedisTemplate<String, Object> objectRedisTemplate;
	private final ChatRoomService chatRoomService;
	private final ChatMessageRepository chatMessageRepository;

	private static final String CHAT_ROOMS = "CHAT_ROOM";
	@Resource(name = "objectRedisTemplate")
	private HashOperations<String, String, ChatRoom> hashOpsChatRoom;

	/**
	 * destination정보에서 roomId 추출
	 */
	public String getRoomId(String destination) {
		int lastIndex = destination.lastIndexOf('/');
		if (lastIndex != -1)
			return destination.substring(lastIndex + 1);
		else
			return "";
	}

	/**
	 * 채팅방에 메시지 발송
	 */
	@Transactional
	public void sendChatMessage(ChatMessage chatMessage) {
		chatMessage.setUserCount(chatRoomService.getUserCount(chatMessage.getRoomId()));
		ChatRoom chatRoom = chatRoomService.findByRoomId(chatMessage.getRoomId());
		String receiver = chatRoomService.findOther(chatRoom.getRoomId(), chatMessage.getSender());

		// 수신자가 채팅방 삭제한 상태
		if(chatRoom.getDeleteStatus(receiver)) {
			// 삭제 취소
			chatRoom.setDeleteStatus(receiver, false);
			hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);	// 레디스 업데이트
		}
		System.out.println(receiver+"의 삭제 여부: "+ chatRoom.getDeleteStatus(receiver));

		if (ChatMessage.MessageType.TALK.equals(chatMessage.getType())) {
			// redis로 메세지 발송
			objectRedisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
			ChatMessage message = ChatMessage.builder()
				.type(ChatMessage.MessageType.TALK)
				.roomId(chatRoom.getRoomId()) // 동일한 roomId 사용
				.sender(chatMessage.getSender()) // 원 메시지의 발신자
				.receiver(receiver)
				.message(chatMessage.getMessage()) // 원 메시지 내용
				.userCount(chatMessage.getUserCount())
				.image(chatMessage.getImage())
				.latitude(0.0f)
				.longitude(0.0f)
				.build();

			chatRoom.setLastMessage(message.getMessage()); // 마지막 메시지 업데이트
			chatRoom.setLastTimestamp(message.getTimestamp()); // 마지막 타임스탬프 업데이트

			hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom); // 레디스에 업데이트
			// DB에 저장
			chatMessageRepository.save(message);
		} else if (ChatMessage.MessageType.IMAGE.equals(chatMessage.getType())) {
			chatMessage.setMessage("");

			ChatMessage message = ChatMessage.builder()
				.type(ChatMessage.MessageType.IMAGE)
				.roomId(chatRoom.getRoomId()) // 동일한 roomId 사용
				.sender(chatMessage.getSender()) // 원 메시지의 발신자
				.receiver(receiver)
				.message("") // 원 메시지 내용
				.userCount(chatMessage.getUserCount())
				.image(chatMessage.getImage())
				.latitude(0.0f)
				.longitude(0.0f)
				.build();

			// redis로 메세지 발송
			objectRedisTemplate.convertAndSend(channelTopic.getTopic(), message);
			hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom); // 레디스에 업데이트
			chatMessageRepository.save(message);
		} else if (ChatMessage.MessageType.LOCATION.equals(chatMessage.getType())) {
			// LOCATION 타입 처리를 위한 코드 추가
			ChatMessage message = ChatMessage.builder()
				.type(ChatMessage.MessageType.LOCATION)
				.roomId(chatRoom.getRoomId()) // 동일한 roomId 사용
				.sender(chatMessage.getSender()) // 원 메시지의 발신자
				.receiver(receiver)
				.message("") // 메시지 내용
				.userCount(chatMessage.getUserCount())
				.latitude(chatMessage.getLatitude()) // 위도 설정
				.longitude(chatMessage.getLongitude()) // 경도 설정
				.build();

			// Redis로 메시지 발송
			objectRedisTemplate.convertAndSend(channelTopic.getTopic(), message);
			hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom); // 레디스에 업데이트
			chatMessageRepository.save(message);
		}
	}

}