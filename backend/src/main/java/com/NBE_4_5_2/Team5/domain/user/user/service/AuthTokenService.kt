package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.global.standard.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthTokenService(
    private val userRepository: UserRepository
) {
    @Value("\${custom.jwt.secret-key}")
    private val keyString: String = ""

    @Value("\${custom.jwt.expire-seconds}")
    private val expireSeconds: Int = 0

    fun generateRefreshToken(): String = UUID.randomUUID().toString()

    fun generateAccessToken(user: User): String {
        return Ut.Jwt.createToken(
            keyString,
            expireSeconds,
            mapOf(
                "id" to user.id,
                "username" to user.username,
                "nickname" to user.nickname,
                "role" to user.role.name
            )
        )
    }

    fun getPayload(accessToken: String?): Map<String, Any?>? {
        if (!Ut.Jwt.isValidToken(keyString, accessToken)) {
            return null
        }

        val payload = Ut.Jwt.getPayload(keyString, accessToken)
        val id = payload["id"] as? String
        val username = payload["username"] as? String
        val nickname = payload["nickname"] as? String
        val roleStr = payload["role"] as? String ?: throw IllegalArgumentException("Role is missing in token")
        val role = Role.valueOf(roleStr)

        return mapOf(
            "id" to id,
            "username" to username,
            "nickname" to nickname,
            "role" to role
        )
    }

    fun getUsernameFromToken(accesstoken: String?): String? {
        val payload = getPayload(accesstoken)
        return payload?.get("username")?.let { username ->
            getNicknameFromName(username as String)
        }
    }

    fun getNicknameFromName(username: String): String? {
        return userRepository.findByUsername(username)?.nickname
    }
}
