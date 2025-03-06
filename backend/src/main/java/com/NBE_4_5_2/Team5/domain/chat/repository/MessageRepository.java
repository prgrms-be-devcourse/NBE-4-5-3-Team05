package com.NBE_4_5_2.Team5.domain.chat.repository;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<ChatMessage, String> {

}
