package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.global.standard.util.Ut
import com.NBE_4_5_2.Team5.global.standard.util.Ut.Jwt.createToken
import com.NBE_4_5_2.Team5.global.standard.util.Ut.Jwt.isValidToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthTokenService(
    private val userRepository: UserRepository,
) {

    @Value("\${custom.jwt.secret-key}")
    private lateinit var keyString: String

    @Value("\${custom.jwt.expire-seconds}")
    private var expireSeconds: Int = 0

    fun generateRefreshToken(): String = UUID.randomUUID().toString()

    fun generateAccessToken(user: User): String =
        createToken(
            keyString,
            expireSeconds,
            mapOf<String, Any>(
                "id" to user.id,
                "username" to user.username,
                "nickname" to user.nickname,
                "role" to user.role.name
            )
        )

    fun getPayload(accessToken: String): Map<String, Any>? {
        if (!isValidToken(keyString, accessToken)) return null

        val payload = Ut.Jwt.getPayload(keyString, accessToken)
        val id = payload["id"] as String
        val username = payload["username"] as String
        val nickname = payload["nickname"] as String
        val roleStr = payload["role"] as String
        val role = Role.valueOf(roleStr)

        return mapOf(
            "id" to id,
            "username" to username,
            "nickname" to nickname,
            "role" to role
        )
    }

    fun getUsernameFromToken(accessToken: String): String? {
        return getPayload(accessToken)?.let { payload ->
            getNicknameFromName((payload["username"] as String))
        }
    }

    fun getNicknameFromName(username: String): String? =
        userRepository.findByUsername(username).orElse(null)?.nickname
}
