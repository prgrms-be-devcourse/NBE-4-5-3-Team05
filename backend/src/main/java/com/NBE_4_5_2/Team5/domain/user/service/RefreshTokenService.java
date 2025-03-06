package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.RefreshToken;
import com.NBE_4_5_2.Team5.domain.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${custom.jwt.expire-seconds}")  // accessToken과 동일한 만료 시간 적용
    private Long expireSeconds;

    // Refresh Token 저장 (expireSeconds 적용)
    public void saveRefreshToken(String userId, String refreshToken) {
        refreshTokenRepository.save(
                new RefreshToken(
                        userId,
                        refreshToken,
                        expireSeconds
                )
        );
    }

    // Refresh Token 조회
    public Optional<RefreshToken> getRefreshToken(String userId) {
        return refreshTokenRepository.findById(userId);
    }

    // Refresh Token 삭제 (로그아웃 시)
    public void deleteRefreshToken(String userId) {
        refreshTokenRepository.deleteById(userId);
    }
}
