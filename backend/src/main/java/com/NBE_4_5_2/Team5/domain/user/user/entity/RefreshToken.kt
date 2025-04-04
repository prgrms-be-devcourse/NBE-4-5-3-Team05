package com.NBE_4_5_2.Team5.domain.user.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash("token")
data class RefreshToken(
    @Id val userId: String,
    // 이메일 인증 여부를 판단하기 위해 refreshToken을 "verified"로 수정하게 되므로 var 선언
    @Indexed var refreshToken: String,
    @TimeToLive val expiration: Long
)