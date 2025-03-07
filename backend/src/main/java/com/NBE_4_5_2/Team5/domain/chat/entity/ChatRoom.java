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
    private String sender;  // 나
    private String receiver;    // 상대
    private long userCount; // 채팅방 인원수

    public ChatRoom(String sender, String receiver) {
        this.roomId = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.name = sender+"와 "+receiver+"의 채팅방";
    }

    public boolean canAccess(String username){
        if (sender == null || receiver == null) {
            return false; // sender나 receiver가 null이면 접근 권한이 없다고 간주
        }
        return sender.equals(username) || receiver.equals(username);
    }
}