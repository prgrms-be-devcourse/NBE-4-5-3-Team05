package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.RedisRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class RedisService(
    private val redisRepository: RedisRepository
) {

    @Value("\${custom.refreshToken.expire-seconds}")
    private var expireSeconds: Long = 0

    companion object {
        private const val REFRESH_TOKEN_KEY = "refreshToken:"
    }

    private fun createRefreshTokenKey(userId: String): String {
        // "refreshToken:userId" 형태의 redis key 생성
        return "${REFRESH_TOKEN_KEY}${userId}"
    }

    /**
     * redis에 userId와 refreshToken 저장 (expireSeconds 적용)
     */
    fun createToken(user: User, refreshToken: String) {
        val token = RefreshToken(
            createRefreshTokenKey(user.id),
            refreshToken,
            expireSeconds
        )
        redisRepository.save(token)
    }

    /**
     * userId로 Token 조회
     */
    fun getTokenByUserId(userId: String): Optional<RefreshToken> =
        redisRepository.findById(createRefreshTokenKey(userId))

    /**
     * refreshToken으로 Token 조회
     */
    fun getTokenByRefreshToken(refreshToken: String): Optional<RefreshToken> =
        redisRepository.findByRefreshToken(refreshToken)

    /**
     * refreshToken 삭제
     * @param userId 삭제할 userId
     * @return 삭제 성공 여부
     */
    fun deleteTokenByUserId(userId: String): Boolean =
        createRefreshTokenKey(userId)
            .takeIf { redisRepository.existsById(it) } // 값이 존재하는경우 createRefreshTokenKey으로 생성된 key를 넘김
            ?.also { redisRepository.deleteById(it) } // 넘겨받은 key 값으로 deleteById 실행
            ?.let { true } ?: false

    /**
     * Refresh Token 삭제
     * @param refreshToken 삭제할 Refresh Token
     */
    fun deleteTokenByRefreshToken(refreshToken: String) {
        redisRepository.deleteByRefreshToken(refreshToken)
    }
}