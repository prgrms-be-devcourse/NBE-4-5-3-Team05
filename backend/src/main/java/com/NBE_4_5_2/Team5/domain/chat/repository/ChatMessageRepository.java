package com.NBE_4_5_2.Team5.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
	List<ChatMessage> findByRoomId(String roomId);

	List<ChatMessage> findByClientAndRoomId(String client, String roomId);
}