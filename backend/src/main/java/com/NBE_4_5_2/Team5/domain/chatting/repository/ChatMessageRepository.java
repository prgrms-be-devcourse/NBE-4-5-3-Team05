package com.NBE_4_5_2.Team5.domain.chatting.repository;


import com.NBE_4_5_2.Team5.domain.chatting.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomId(String chatRoomId);
}
