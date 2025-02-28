package com.NBE_4_5_2.Team5.domain.chatting.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
public class ChatMessage {
    @Id
    private String id;
    private String chatRoomId;
    private String sender;  // 나
    private String receiver;    // 상대
    private String message;

    @Builder
    public ChatMessage(String chatRoomId, String sender, String receiver, String message) {
        this.id = UUID.randomUUID().toString(); // UUID로 ID 생성
        this.chatRoomId = chatRoomId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }
}

