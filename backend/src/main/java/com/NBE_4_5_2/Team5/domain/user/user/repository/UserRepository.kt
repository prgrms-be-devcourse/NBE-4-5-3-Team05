package com.NBE_4_5_2.Team5.domain.user.user.repository

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByNickname(nickname: String): User?
    fun findAllByRole(role: Role): List<User>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun existsByNickname(nickname: String): Boolean
}