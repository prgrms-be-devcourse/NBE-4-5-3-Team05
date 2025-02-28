package com.NBE_4_5_2.Team5.domain.chatting.dto;

import lombok.Data;

@Data
public class ChattingMessageRequset {
    private String roomId;
    private String message;
    private String senderId;    // 사실상 내 아이디
}
