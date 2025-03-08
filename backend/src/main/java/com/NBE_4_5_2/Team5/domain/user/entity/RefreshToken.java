package com.NBE_4_5_2.Team5.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("token")
public class RefreshToken {

    @Id
    private String userId;

    // refreshToken으로 조회할 수 있도록 인덱스 추가
    @Indexed
    private String refreshToken;

    @TimeToLive
    private Long expiration;
}
