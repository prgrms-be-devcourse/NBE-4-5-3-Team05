package com.NBE_4_5_2.Team5.domain.chat.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    @Id
    private String id;
    private String roomId;

    private String name;
    private String sender;  // 나
    private String receiver;    // 상대
    private String client;    // 개별 저장소
    private long userCount; // 채팅방 인원수
    private String lastMessage;
    private String lastTimestamp;

    public ChatRoom(String sender, String receiver) {
        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.name = receiver;
        this.userCount = 2;
    }
}