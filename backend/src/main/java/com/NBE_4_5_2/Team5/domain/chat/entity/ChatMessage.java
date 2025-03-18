package com.NBE_4_5_2.Team5.domain.chat.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@Entity
@NoArgsConstructor
public class ChatMessage {

	@Id
	private String messageId = UUID.randomUUID().toString();
	private MessageType type; // 메시지 타입
	private String roomId; // 방번호
	private String client;  // 개별 저장소 클라이언트
	private String sender; // 메시지 보낸사람
	private String message; // 메시지
	private String image;
	private long userCount; // 채팅방 인원수, 채팅방 내에서 메시지가 전달될때 인원수 갱신시 사용
	private String timestamp;
	private Float latitude = 0.0f;
	private Float longitude = 0.0f;

	public ChatMessage(MessageType type, String roomId, String client, String sender, String message, String image,
		long userCount, Float latitude, Float longitude) {
		this.type = type;
		this.roomId = roomId;
		this.client = client;
		this.sender = sender;
		this.message = message;
		this.image = image;
		this.userCount = userCount;
		this.timestamp = formatTimestamp(LocalDateTime.now());
		this.latitude = latitude;
		this.longitude = longitude;
	}

	// 메시지 타입 : 입장, 퇴장, 채팅, 이미지 추가
	public enum MessageType {
		ENTER, QUIT, TALK, IMAGE, LOCATION
	}

	public String formatTimestamp(LocalDateTime timestamp) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return timestamp.format(formatter);
	}
}