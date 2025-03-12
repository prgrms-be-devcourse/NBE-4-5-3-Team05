package com.NBE_4_5_2.Team5.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private String postId;
    private String roomId;
    private String name;
    private long userCount;

}
