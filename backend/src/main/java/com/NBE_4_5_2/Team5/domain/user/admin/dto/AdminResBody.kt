package com.NBE_4_5_2.Team5.domain.user.admin.dto

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User


data class AdminResBody(val id: String, val nickname: String, val role: Role) {
    constructor(admin: User) : this(admin.id, admin.nickname, admin.role)
}
