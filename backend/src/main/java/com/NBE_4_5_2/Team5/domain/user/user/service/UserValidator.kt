package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import com.NBE_4_5_2.Team5.global.exception.user.UserNotFoundException
import com.NBE_4_5_2.Team5.global.exception.user.WrongPasswordException
import com.NBE_4_5_2.Team5.global.exception.validation.AlreadyUsedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserValidator(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService
) {

    fun duplicate(username: String, nickname: String) {
        if (userRepository.existsByUsername(username)) {
            throw AlreadyUsedException("409-1", "이미 사용중인 아이디입니다.")
        }
        if (userRepository.existsByNickname(nickname)) {
            throw AlreadyUsedException("409-3", "이미 사용중인 닉네임입니다.")
        }
    }

    fun credentials(username: String, password: String): User {
        val user = userRepository.findByUsername(username)
            ?: throw UserNotFoundException("401-1", "잘못된 아이디입니다.")
        if (!passwordEncoder.matches(password, user.password)) {
            throw WrongPasswordException("401-2", "비밀번호가 일치하지 않습니다.")
        }
        return user
    }

    fun emailVerified(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw ServiceException("409-2", "이미 사용중인 이메일입니다.")
        }
        // 해당 이메일에 대한 인증이 완료되었는지 검증
        val verificationCode = emailService.getVerificationCode(email)
        if (verificationCode == null || verificationCode != "verified") {
            throw ServiceException("409", "이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요.")
        }
    }
}
