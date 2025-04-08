package com.NBE_4_5_2.Team5.domain.chat.dto

data class MessageDto(
    var messageId: String,
    var sender: String,
    var message: String,
    var image: String,
    var latitude:Float,
    var longitude:Float,
    var timestamp: String,
    var lastMessage: String,
    var lastTimestamp: String,
)