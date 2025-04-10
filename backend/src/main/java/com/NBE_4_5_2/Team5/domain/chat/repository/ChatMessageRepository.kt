package com.NBE_4_5_2.Team5.domain.chat.repository

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, String> {
    fun findBy_roomId(roomId: String): List<ChatMessage>
}