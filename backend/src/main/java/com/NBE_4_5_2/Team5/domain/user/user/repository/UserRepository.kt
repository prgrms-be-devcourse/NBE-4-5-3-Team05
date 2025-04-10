package com.NBE_4_5_2.Team5.domain.user.user.repository

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, String> {
    fun findByUsername(username: String): Optional<User>
    fun findAllByRole(role: Role): List<User?>
    fun findAllByRoleIn(roles: List<Role>, pageable: Pageable): Page<User>
    fun existsByEmail(email: String): Boolean // 이메일 중복 체크
    fun existsByUsername(username: String): Boolean
    fun existsByNickname(nickname: String): Boolean
}
