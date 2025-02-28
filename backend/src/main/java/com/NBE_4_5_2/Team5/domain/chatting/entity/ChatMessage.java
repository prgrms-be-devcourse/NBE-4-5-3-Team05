package com.NBE_4_5_2.Team5.domain.chatting.entity;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ChatMessage {
    private String chatRoomId;
    private String sender;
    private String message;
}

