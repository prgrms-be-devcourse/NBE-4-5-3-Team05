package com.NBE_4_5_2.Team5.domain.chat.repository;

import java.util.List;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
	List<ChatMessage> findByRoomId(String roomId);

	List<ChatMessage> findByClientAndRoomId(String client, String roomId);

	ChatMessage findTopByOrderByTimestampDesc();
}