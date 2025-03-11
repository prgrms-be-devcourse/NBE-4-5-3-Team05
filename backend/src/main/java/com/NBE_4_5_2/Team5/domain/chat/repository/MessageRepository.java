package com.NBE_4_5_2.Team5.domain.chat.repository;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findAllByClientAndRoomId(String client, String roomId);

    void deleteAllByClient(String id);
}
