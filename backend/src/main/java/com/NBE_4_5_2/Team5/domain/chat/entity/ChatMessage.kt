package com.NBE_4_5_2.Team5.domain.chat.entity

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Entity
class ChatMessage (
    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private var _type: MessageType = MessageType.TALK,
    @Column(name="room_id")
    private var _roomId: String="",
    @Column(name = "sender")
    private var _sender: String="",
    @Column(name = "receiver")
    private val _receiver: String="",
    @Column(name = "message")
    private var _message: String="",
    @Column(name = "image")
    @JsonProperty("image")
    private var _image: String="",
    @Column(name = "user_count")
    private var _userCount: Long=0,
    @Column(name = "latitude")
    @JsonProperty("latitude")
    private var _latitude: Float=0.0f,
    @Column(name = "longitude")
    @JsonProperty("longitude")
    private var _longitude: Float=0.0f
) {
    @Id
    @Column(name = "message_id")
    private val _messageId = UUID.randomUUID().toString()
    @Column(name = "timestamp")
    private var _timestamp: String = formatTimestamp(LocalDateTime.now())

    @ElementCollection
    private val deleteStatus: MutableMap<String, Boolean> = HashMap()

    fun getMessageId(): String {
        return _messageId
    }
    fun getType(): MessageType {
        return _type
    }
    fun setType(type: MessageType) {
        _type=type
    }
    fun getRoomId(): String {
        return _roomId
    }
    fun setRoomId(value: String) {
        _roomId=value
    }
    fun getSender(): String {
        return _sender
    }
    fun setSender(sender: String) {
        _sender=sender
    }
    fun getReceiver(): String {
        return _receiver
    }
    fun getMessage(): String {
        return _message
    }
    fun setMessage(message: String) {
        _message = message
    }
    fun getTimestamp(): String {
        return _timestamp
    }
    fun getImage(): String {
        return _image
    }
    fun getUserCount(): Long {
        return _userCount
    }
    fun setUserCount(userCount: Long) {
        _userCount=userCount
    }
    fun getLatitude(): Float {
        return _latitude
    }
    fun getLongitude(): Float {
        return _longitude
    }

    init {
        deleteStatus[_sender] = false
        deleteStatus[_receiver] = false
    }

    // 메시지 타입 : 입장, 퇴장, 채팅, 이미지 추가
    enum class MessageType {
        ENTER, QUIT, TALK, IMAGE, LOCATION
    }

    companion object{
        fun formatTimestamp(timestamp: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return timestamp.format(formatter)
        }
    }

    fun setDeleteStatus(username: String, status: Boolean) {
        deleteStatus[username] = status
    }

    fun getDeleteStatus(username: String?): Boolean? {
        return deleteStatus[username]
    }
    override fun toString(): String {
        return "ChatMessage(messageId='$_messageId', type='$_type', roomId='$_roomId', sender='$_sender', receiver='$_receiver', message='$_message', image='$_image', userCount=$_userCount, latitude=$_latitude, longitude=$_longitude, timestamp='$_timestamp')"
    }
}
