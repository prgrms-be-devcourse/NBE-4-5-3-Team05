package com.NBE_4_5_2.Team5.domain.chat.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    @Id
    private String roomId;

    private String name;
    private long userCount; // 채팅방 인원수

//    public static ChatRoom create(String name) {
//        ChatRoom chatRoom = new ChatRoom();
//        chatRoom.roomId = UUID.randomUUID().toString();
//        chatRoom.name = name;
//        return chatRoom;
//    }

    public ChatRoom(String name) {
        this.roomId = UUID.randomUUID().toString();
        this.name = name;
    }
}