package com.NBE_4_5_2.Team5.domain.chat.entity

import jakarta.persistence.Id
import java.io.Serializable
import java.util.*

class ChatRoom(// 나
    private val sender: String, // 상대
    private val receiver: String
) : Serializable {
    @Id
    val id : String = UUID.randomUUID().toString()

    var roomId: String = ""

    var name = receiver
    var client: String = "" // 개별 저장소
    var userCount: Long = 0 // 채팅방 인원수
    var lastMessage: String = ""
    var lastTimestamp: String = ""
    var isDelete: MutableMap<String, Boolean> = HashMap() // 논리적 삭제(userNickname,True/False)

    init {
        isDelete[sender] = false
        isDelete[receiver] = false
    }

    fun getSender() : String {
        return sender;
    }
    fun getReceiver() : String {
        return receiver;
    }

    fun setDeleteStatus(username: String, status: Boolean) {
        isDelete[username] = status
    }

    fun getDeleteStatus(username: String): Boolean {
        return isDelete[username]!!
    }

    companion object {
        private const val serialVersionUID = 6494678977089006639L
    }
}