package com.NBE_4_5_2.Team5.domain.user.user.dto

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import java.time.LocalDateTime

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val nickname: String,
    val address: String?,
    val profileUrl: String?,
    val role: Role,
    val cash: Int,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val blocked: Boolean,
    val blockedCount: Int
) {
    companion object {
        fun fromEntity(user: User): UserDto = UserDto(
            id = user.id,
            username = user.username,
            email = user.email,
            nickname = user.nickname,
            address = user.address,
            profileUrl = user.profileUrl,
            role = user.role,
            cash = user.cash,
            createdAt = user.createdAt,
            modifiedAt = user.modifiedAt,
            blocked = user.blocked,
            blockedCount = user.blockedCount
        )
    }
}
