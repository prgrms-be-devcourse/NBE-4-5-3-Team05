package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.RedisRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class RedisService(
    private val redisRepository: RedisRepository
) {
    @Value("\${custom.refreshToken.expire-seconds}")
    private val expireSeconds: Long = 0L

    /**
     * Redis에 userId와 refreshToken을 저장 (expireSeconds 적용)
     */
    fun createToken(user: User, refreshToken: String) {
        val key = REFRESH_TOKEN_KEY + user.id
        val token = RefreshToken.builder()
            .userId(key)
            .refreshToken(refreshToken)
            .expiration(expireSeconds)
            .build()
        redisRepository.save(token)
    }

    /**
     * userId로 Token 조회
     */
    fun getTokenByUserId(userId: String): Optional<RefreshToken> {
        val key = REFRESH_TOKEN_KEY + userId
        return redisRepository.findById(key)
    }

    /**
     * refreshToken으로 Token 조회
     */
    fun getTokenByRefreshToken(refreshToken: String): RefreshToken? {
        return redisRepository.findByRefreshToken(refreshToken)
    }

    /**
     * userId에 해당하는 refreshToken 삭제
     * @return 삭제 성공 여부
     */
    fun deleteTokenByUserId(userId: String): Boolean {
        val key = REFRESH_TOKEN_KEY + userId
        if (!redisRepository.existsById(key)) {
            return false
        }
        redisRepository.deleteById(key)
        return true
    }

    /**
     * refreshToken 삭제
     */
    fun deleteTokenByRefreshToken(refreshToken: String) {
        redisRepository.deleteByRefreshToken(refreshToken)
    }

    companion object {
        private const val REFRESH_TOKEN_KEY = "refreshToken:"
    }
}
