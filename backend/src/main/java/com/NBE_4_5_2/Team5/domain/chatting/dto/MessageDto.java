package com.NBE_4_5_2.Team5.domain.chatting.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    // 입장, 퇴장, 채팅
    public enum MessageType {
        JOIN, LEAVE, TALK
    }

    private MessageType type;
    private String message;
    private Long chatRoomId;
}
