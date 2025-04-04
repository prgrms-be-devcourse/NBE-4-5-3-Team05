package com.NBE_4_5_2.Team5.domain.user.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash("token")
data class RefreshToken(
    @Id
    var userId: String = "",
    @Indexed
    var refreshToken: String = "",
    @TimeToLive
    var expiration: Long = 0L
)
