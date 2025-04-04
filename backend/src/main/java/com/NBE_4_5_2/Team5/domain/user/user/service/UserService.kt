package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserUpdateRequest
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import com.NBE_4_5_2.Team5.global.Rq
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotFoundException
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotValidException
import com.NBE_4_5_2.Team5.global.exception.security.TokenNotFoundException
import com.NBE_4_5_2.Team5.global.security.SecurityUser
import jakarta.transaction.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserService(
    private val emailService: EmailService,
    private val userRepository: UserRepository,
    private val authTokenService: AuthTokenService,
    private val redisService: RedisService,
    private val passwordEncoder: PasswordEncoder,
    private val userValidator: UserValidator,
    private val rq: Rq
) {

    fun createUser(
        username: String,
        password: String,
        email: String,
        nickname: String,
        address: String?,
        profileUrl: String?
    ): User {
        userValidator.duplicate(username, nickname)
        userValidator.emailVerified(email)
        val user = User(
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            nickname = nickname,
            address = address,
            profileUrl = profileUrl,
            cash = 0,
            role = Role.USER,
            blocked = false,
            blockedCount = 0,
            purchasedProducts = mutableListOf(),
            writtenProducts = mutableListOf(),
            wroteComments = mutableListOf()
        )
        return userRepository.save(user)
    }

    fun loginUser(username: String, password: String): User {
        return userValidator.credentials(username, password)
    }

    fun logoutUser(userIdentity: User) {
        val isDeleted = redisService.deleteTokenByUserId(userIdentity.id)
        if (!isDeleted) {
            rq.refreshToken.ifPresent { redisService.deleteTokenByRefreshToken(it) }
        }
    }

    fun getUserById(id: String): Optional<User> = userRepository.findById(id)

    fun getUserByUsername(username: String): User? = userRepository.findByUsername(username)

    fun getUserByAccessToken(accessToken: String): Optional<User> {
        val payload = authTokenService.getPayload(accessToken) ?: return Optional.empty()
        val id = payload["id"] as String
        val username = payload["username"] as String
        val nickname = payload["nickname"] as String
        val role = payload["role"] as Role
        val user = User(
            id = id,
            username = username,
            password = "",  // 빈 문자열로 처리
            email = "",     // 빈 문자열로 처리
            nickname = nickname,
            address = null,
            profileUrl = null,
            cash = 0,
            role = role,
            blocked = false,
            blockedCount = 0,
            purchasedProducts = mutableListOf(),
            writtenProducts = mutableListOf(),
            wroteComments = mutableListOf()
        )
        return Optional.of(user)
    }

    fun saveRefreshToken(user: User, refreshToken: String) {
        redisService.createToken(user, refreshToken)
    }

    fun refreshAccessToken(refreshToken: String): String {
        val tokenByRefreshToken = redisService.getTokenByRefreshToken(refreshToken)
            ?: throw ServiceException("401-1", "유효하지 않은 RefreshToken입니다.")
        val userId = tokenByRefreshToken.userId.substring("refreshToken:".length)
        redisService.deleteTokenByUserId(userId)
        return userRepository.findById(userId)
            .map { authTokenService.generateAccessToken(it) }
            .orElseThrow { ServiceException("404-1", "존재하지 않는 회원의 refreshToken입니다.") }
    }

    fun generateAuthtoken(user: User): AuthToken {
        val refreshToken = authTokenService.generateRefreshToken()
        val accessToken = authTokenService.generateAccessToken(user)
        saveRefreshToken(user, refreshToken)
        return AuthToken(refreshToken, accessToken)
    }

    fun generateAuthTokenAsString(user: User): String {
        val authToken = generateAuthtoken(user)
        return "${authToken.refreshToken} ${authToken.accessToken}"
    }

    fun getRefreshTokenByUserId(userId: String): String {
        return redisService.getTokenByUserId(userId)
            .map { it.refreshToken }
            .orElseThrow { TokenNotFoundException("401-1", "로그인이 필요합니다.") }
    }

    fun getUserByRefreshToken(refreshToken: String): User? {
        val token = redisService.getTokenByRefreshToken(refreshToken) ?: return null
        val userId = token.userId.substring("refreshToken:".length)
        return userRepository.findById(userId).orElse(null)
    }

    fun count(): Long = userRepository.count()

    val userIdentity: User
        get() {
            val authentication = SecurityContextHolder.getContext().authentication
                ?: throw AuthenticationNotFoundException("401-2", "로그인이 필요합니다.")
            val principal = authentication.principal as? SecurityUser
                ?: throw AuthenticationNotValidException("401-3", "잘못된 인증 정보입니다")
            return User(
                id = principal.id,
                username = principal.username,
                password = "", // 빈 문자열 처리
                email = "",    // 빈 문자열 처리
                nickname = principal.nickname,
                address = "",
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

    @Transactional
    fun updateMyProfile(user: User, updateRequest: UserUpdateRequest): UserDto {
        val updateNickname = updateRequest.nickname
        if (updateNickname != null && updateNickname != user.nickname) {
            if (userRepository.existsByNickname(updateNickname)) {
                throw ServiceException("400-NICKNAME-ALREADY-EXISTS", "이미 사용중인 닉네임입니다.")
            }
            user.nickname = updateNickname
        }
        if (updateRequest.address != null) {
            user.address = updateRequest.address
        }
        if (updateRequest.profileUrl != null) {
            user.profileUrl = updateRequest.profileUrl
        }
        val updateEmail = updateRequest.email
        if (updateEmail != null && updateEmail != user.email) {
            userValidator.emailVerified(updateEmail)
            user.email = updateEmail
        }
        return UserDto.fromEntity(user)
    }

    @Transactional
    fun deleteMyProfile(user: User) {
        userRepository.delete(user)
    }

    fun sendAuthenticationCode(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw ServiceException("400-1", "이미 사용중인 이메일입니다.")
        }
        emailService.sendAuthenticationCode(email)
        emailService.checkBouncedEmail(email)
    }

    fun verifyAuthenticationCode(email: String, code: String) {
        val savedCode = emailService.getVerificationCode(email)
        val verified = emailService.verifyAuthenticationCode(code, savedCode)
        if (!verified) {
            throw ServiceException("400-1", "인증코드가 틀렸습니다.")
        }
        emailService.saveVerificationCode(email, "verified")
    }
}
