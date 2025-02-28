package com.NBE_4_5_2.Team5.domain.chatting.entity;

import com.NBE_4_5_2.Team5.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
public class ChatRoom {
    @Id
    private String id;

    @ManyToOne
    private Users sender;   // 발신자 (나)
    @ManyToOne
    private Users receiver; // 수신자 (상대방)
    private String roomName;    // 방 이름: 상대방 이름으로

    @Builder
    public ChatRoom(Users receiver, Users sender) {
        this.id = "chatRoom-" + UUID.randomUUID();
        this.receiver = receiver;
        this.sender = sender;
        this.roomName = receiver.getUsername()+"와의 채팅방";
    }
}