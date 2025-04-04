package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotValidException
import com.NBE_4_5_2.Team5.global.security.SecurityUser
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class UserAuthService(
    private val userService: UserService
) {

    fun setLogin(actor: User) {
        val user: UserDetails = SecurityUser(actor.id, actor.username, "", "", actor.role, listOf())
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }

    fun getRealActor(actor: User): User {
        return userService.getUserById(actor.id)
            .orElseThrow { RuntimeException("User not found with id ${actor.id}") }
    }

    val userIdentity: User
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication == null || authentication is AnonymousAuthenticationToken) {
                throw AuthenticationNotValidException("401-1", "로그인이 필요합니다.")
            }
            val principal = authentication.principal as? SecurityUser
                ?: throw AuthenticationNotValidException("401-3", "잘못된 인증 정보입니다")
            // builder 대신 primary constructor를 사용하여 User 객체 생성
            return User(
                id = principal.id,
                username = principal.username,
                password = "", // 기본값 처리
                email = "",    // 기본값 처리
                nickname = principal.nickname,
                address = null,
                profileUrl = null,
                cash = 0,
                role = principal.role,
                blocked = false,
                blockedCount = 0,
                purchasedProducts = mutableListOf(),
                writtenProducts = mutableListOf(),
                wroteComments = mutableListOf()
            )
        }

    val me: UserDto
        get() = UserDto(getRealActor(userIdentity))
}
