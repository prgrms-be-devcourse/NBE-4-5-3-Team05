package com.NBE_4_5_2.Team5.domain.chat.dto

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom

class ChatRoomDto(
    val id : String,
    val roomId : String,
    val name : String,
    val userCount : Long,
    val lastMessage: String,
    val messageType: ChatMessage.MessageType,
    val lastTimestamp: String,
    val other: String
) {
    constructor(chatRoom: ChatRoom, lastMessage: String, messageType: ChatMessage.MessageType, lastTimestamp: String, other: String) : this(
        id = chatRoom.id,
        roomId = chatRoom.roomId,
        name = chatRoom.name,
        userCount = chatRoom.userCount,
        lastMessage = lastMessage,
        messageType = messageType,
        lastTimestamp = lastTimestamp,
        other = other
    )
}
