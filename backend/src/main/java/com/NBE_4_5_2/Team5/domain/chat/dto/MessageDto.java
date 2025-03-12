package com.NBE_4_5_2.Team5.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String sender;
    private String message;
    private String image;
    private String timestamp;
}
