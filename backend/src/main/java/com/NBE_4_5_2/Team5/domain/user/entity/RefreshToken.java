package com.NBE_4_5_2.Team5.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String userId;
    private String token;

    @TimeToLive
    private Long expiration;
}

