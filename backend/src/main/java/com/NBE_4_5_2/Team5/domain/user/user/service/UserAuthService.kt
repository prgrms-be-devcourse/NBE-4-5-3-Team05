package com.NBE_4_5_2.Team5.domain.user.user.service

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

    fun setLogout() {
        SecurityContextHolder.clearContext()
    }

    fun getRealActor(actor: User): User {
        return userService.getUserById(actor.id).get()
    }

    val userIdentity: User
        get() {
            val authentication = SecurityContextHolder.getContext().authentication

            /**
             * Spring Security에서는 인증되지 않은 사용자를 자동으로 `AnonymousAuthenticationToken`으로 설정
             * 따라서 `authentication == null`이 아닐 수 있으므로 추가적인 확인을 진행함
             */
            if (authentication == null || authentication is AnonymousAuthenticationToken) {
                throw AuthenticationNotValidException("401-1", "로그인이 필요합니다.")
            }

            val principal = authentication.principal as? SecurityUser
                ?: throw AuthenticationNotValidException("401-3", "잘못된 인증 정보입니다")

            val user = principal

            return User(
                user.id,
                user.username,
                user.nickname,
                user.role
            )
        }
}
