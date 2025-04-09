package com.NBE_4_5_2.Team5.domain.user.user.dto

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import java.time.LocalDateTime

class UserDto(
    var id: String,
    var username: String,
    var email: String,
    var nickname: String,
    var address: String,
    var profileUrl: String,
    var role: Role,
    var cash: Int,
    var createdAt: LocalDateTime,
    var modifiedAt: LocalDateTime,
    var blocked: Boolean,
    var blockedCount: Int,
    var latitude: Float,
    var longitude: Float,
){
    constructor(admin: User) : this(
        id = admin.id,
        username = admin.username,
        email = admin.email,
        nickname = admin.nickname,
        address = admin.address,
        profileUrl = admin.profileUrl,
        role = admin.role,
        cash = admin.cash,
        createdAt = admin.createdDate,
        modifiedAt = admin.modifiedDate,
        blocked = admin.blocked,
        blockedCount = admin.blockedCount,
        latitude = admin.latitude,
        longitude = admin.longitude,
    )

    companion object {
        fun fromEntity(user: User): UserDto {
            return UserDto(user)
        }
    }
}

