package com.NBE_4_5_2.Team5.domain.user.repository;

import com.NBE_4_5_2.Team5.domain.user.entity.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefreshTokenRepositoryTest {

    @Value("${custom.jwt.expire-seconds}") // application.yml 설정값 자동 주입
    private Long expireSeconds;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("Redis에 RefreshToken을 저장하고 조회 테스트")
    void redisBasicTest() {

        RefreshToken refreshToken = RefreshToken
                .builder()
                .userId("user-12345678")
                .token("token-12345678")
                .expiration(expireSeconds)
                .build();

        refreshTokenRepository.save(refreshToken);


        RefreshToken found = refreshTokenRepository.findById("user-12345678").get();


        assertThat(found.getToken()).isEqualTo("token-12345678");
        assertThat(found.getUserId()).isEqualTo("user-12345678");
        assertThat(found.getExpiration()).isEqualTo(expireSeconds);
    }
}