package com.NBE_4_5_2.Team5.domain.chat.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ChatMessage {
	@Id
	private String messageId = UUID.randomUUID().toString();

	private MessageType type; // 메시지 타입
	private String roomId; // 방번호
	private String sender; // 메시지 보낸사람
	private String receiver;
	private String message; // 메시지
	private String image;
	private long userCount; // 채팅방 인원수, 채팅방 내에서 메시지가 전달될때 인원수 갱신시 사용
	private String timestamp;
	private Float latitude = 0.0f;
	private Float longitude = 0.0f;

	@ElementCollection
	private Map<String, Boolean> deleteStatus = new HashMap<>();

	@Builder
	public ChatMessage(MessageType type, String roomId, String sender, String receiver, String message, String image,
		long userCount, Float latitude, Float longitude) {
		messageId = UUID.randomUUID().toString();
		this.type = type;
		this.roomId = roomId;
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.image = image;
		this.userCount = userCount;
		this.timestamp = formatTimestamp(LocalDateTime.now());
		this.latitude = latitude;
		this.longitude = longitude;

		deleteStatus.put(sender, false);
		deleteStatus.put(receiver, false);
	}

	// 메시지 타입 : 입장, 퇴장, 채팅, 이미지 추가
	public enum MessageType {
		ENTER, QUIT, TALK, IMAGE, LOCATION
	}

	public String formatTimestamp(LocalDateTime timestamp) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return timestamp.format(formatter);
	}

	public void setDeleteStatus(String username,boolean status) {
		this.deleteStatus.put(username,status);
	}

	public Boolean getDeleteStatus(String username) {
		return this.deleteStatus.get(username);
	}

}