package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.RefreshToken;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {

    private static final String REFRESH_TOKEN_KEY = "refreshToken:";

    private final RedisRepository redisRepository;

    @Value("${custom.refreshToken.expire-seconds}")
    private Long expireSeconds;

    /**
     * redis에 userId와 refreshToken 저장 (expireSeconds 적용)
     */
    public void createToken(User user, String refreshToken) {
        String key = REFRESH_TOKEN_KEY + user.getId();

        RefreshToken token = RefreshToken.builder()
                .userId(key)
                .refreshToken(refreshToken)
                .expiration(expireSeconds)
                .build();

        redisRepository.save(token);
    }

    /**
     * userId로 Token 조회
     */
    public Optional<RefreshToken> getTokenByUserId(String userId) {
        String key = REFRESH_TOKEN_KEY + userId;
        return redisRepository.findById(key);
    }

    /**
     * refreshToken으로 Token 조회
     */
    public Optional<RefreshToken> getTokenByRefreshToken(String refreshToken) {
        return redisRepository.findByRefreshToken(refreshToken);
    }

    /**
     * refreshToken 삭제
     * @param userId 삭제할 userId
     * @return 삭제 성공 여부
     */
    public boolean deleteTokenByUserId(String userId) {
        String key = REFRESH_TOKEN_KEY + userId;

        if (!redisRepository.existsById(key)) {
            return false;
        }

        redisRepository.deleteById(key);
        return true;
    }

    /**
     * Refresh Token 삭제
     * @param refreshToken 삭제할 Refresh Token
     */
    public void deleteTokenByRefreshToken(String refreshToken) {
        redisRepository.deleteByRefreshToken(refreshToken);
    }

}