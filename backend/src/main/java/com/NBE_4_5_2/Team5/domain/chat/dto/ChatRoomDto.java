package com.NBE_4_5_2.Team5.domain.chat.dto;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
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
    private String lastMessage;
    private String lastTimestamp;
    private String other;

    public ChatRoomDto(ChatRoom chatRoom) {
        this.postId=chatRoom.getId();
        this.roomId=chatRoom.getRoomId();
        this.name=chatRoom.getName();
        this.userCount=chatRoom.getUserCount();
    }

}
