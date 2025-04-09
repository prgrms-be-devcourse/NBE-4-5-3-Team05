package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken
import com.NBE_4_5_2.Team5.domain.user.user.dto.LocationRequest
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto.Companion.fromEntity
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserUpdateRequest
import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import com.NBE_4_5_2.Team5.global.rq.Rq
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotValidException
import com.NBE_4_5_2.Team5.global.exception.security.TokenNotFoundException
import com.NBE_4_5_2.Team5.global.exception.user.UserNotFoundException
import com.NBE_4_5_2.Team5.global.security.SecurityUser
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val redisService: RedisService,
    private val authTokenService: AuthTokenService,
    private val userValidator: UserValidator,
    private val passwordEncoder: PasswordEncoder,
    private val rq: Rq
) {

    @Transactional
    fun createUser(
        username: String, password: String, email: String,
        nickname: String, address: String, profileUrl: String
    ): User {
        userValidator.duplicate(username, nickname)
        userValidator.emailVerified(email)

        val user = User().apply {
            this.username = username
            this.password = passwordEncoder.encode(password)
            this.email = email
            this.nickname = nickname
            this.address = address
            this.profileUrl = profileUrl
            this.role = Role.USER
        }
        return userRepository.save(user)
    }

    /**
     * 로그인 검증
     *
     * @param username 사용자 아이디
     * @param password 사용자 비밀번호
     * @return 검증된 User 객체
     */
    fun loginUser(username: String, password: String): User {
        return userValidator.credentials(username, password)
    }

    /**
     * 로그아웃 처리 (redis에서 refreshToken 제거)
     *
     *
     * redis에 저장된 refreshToken을 제거합니다.
     * 1. 로그인된 authentication의 UserId를 기반으로 삭제합니다.
     * 2. 삭제 실패 시 사용자가 보유한 refreshToken을 기반으로 다시 삭제합니다.
     */
    fun logoutUser(userIdentity: User) {
        redisService.deleteTokenByUserId(userIdentity.id)
            .takeIf { deleted -> !deleted }  // 삭제되지 않았을 경우에만 실행
            ?.also {
                rq.refreshToken.ifPresent(redisService::deleteTokenByRefreshToken)
            }
    }

    fun getUserById(id: String): Optional<User> {
        return userRepository.findById(id)
    }

    fun getUserByUsername(username: String): Optional<User> {
        return userRepository.findByUsername(username)
    }

    /**
     * AccessToken payload에 저장된 id와 username, role만을 가진 User 객체를 반환
     */
    fun getUserByAccessToken(accessToken: String): Optional<User> {
        val payload = authTokenService.getPayload(accessToken)
            ?: return Optional.empty()

        val id = payload["id"] as String
        val username = payload["username"] as String
        val nickname = payload["nickname"] as String
        val role = payload["role"] as Role

        return Optional.of(
            User(
                id,
                username,
                nickname,
                role
            )
        )
    }

    /**
     * Redis에 refreshToken 저장
     *
     * @param user         로그인한 사용자
     * @param refreshToken 저장할 refreshToken
     * 기존에 userId로 저장된 refreshToken이 존재할 경우 덮어 씌웁니다.
     */
    fun saveRefreshToken(user: User, refreshToken: String) {
        redisService.createToken(user, refreshToken)
    }

    /**
     * refreshToken 검증
     *
     * @param refreshToken 검증할 refreshToken
     * @throws ServiceException 사용자의 userId로 된 refreshToken이 존재하지 않거나 값이 일치하지 않을 경우
     */
    fun refreshAccessToken(refreshToken: String): String {

        return redisService.getTokenByRefreshToken(refreshToken)
            .orElseThrow { ServiceException("401-1", "유효하지 않은 RefreshToken입니다.") }
            .userId.substring("refreshToken:".length)
            .also { redisService.deleteTokenByUserId(it) }
            .let {
                userRepository.findById(it)
                    .orElseThrow { UserNotFoundException("404-1", "존재하지 않는 회원입니다.") }
                    ?.let { authTokenService.generateAccessToken(it) }
                    ?: throw ServiceException("404-1", "존재하지 않는 회원의 refreshToken입니다.")
            }
    }

    /**
     * User 정보로 AuthToken을 생성하여 반환
     * refreshToken은 redis에 저장됨
     *
     * @param user 로그인한 사용자
     * @return refreshToken, accessToken을 담은 AuthToken 객체
     */
    fun generateAuthtoken(user: User): AuthToken {
        val refreshToken = authTokenService.generateRefreshToken()
        val accessToken = authTokenService.generateAccessToken(user)

        saveRefreshToken(user, refreshToken)
        return AuthToken(refreshToken, accessToken)
    }

    /**
     * User 정보로 AuthToken을 생성하여 String 형태로 반환
     *
     * @param user 로그인한 사용자
     * @return refreshToken, accessToken을 공백으로 구분한 문자열
     * refreshToken은 redis에 저장됨
     */
    fun generateAuthTokenAsString(user: User): String {
        val authToken = generateAuthtoken(user)
        return "${authToken.refreshToken} ${authToken.accessToken}"
    }

    fun getRefreshTokenByUserId(userId: String): String {
        return redisService.getTokenByUserId(userId)
            .map(RefreshToken::refreshToken)
            .orElseThrow { TokenNotFoundException("401-1", "로그인이 필요합니다.") }
    }

    /**
     * refreshToken을 기반으로 User 객체를 반환
     *
     * @param refreshToken 검증할 refreshToken
     * redis에 존재하지 않을 경우 Optional.empty() 반환
     * @return refreshToken을 기반으로 찾은 User 객체
     */
    fun getUserByRefreshToken(refreshToken: String): Optional<User> =
        redisService
            .getTokenByRefreshToken(refreshToken)
            .flatMap { token -> userRepository.findById(token.userId.substring("refreshToken:".length)) }

    val userIdentity: User // TODO: UserAuthService와 코드 중복, 전체적으로 통일 필요
        get() {
            val authentication = SecurityContextHolder.getContext().authentication

            /**
             * Spring Security에서는 인증되지 않은 사용자를 자동으로 `AnonymousAuthenticationToken`으로 설정
             * 따라서 `authentication == null`이 아닐 수 있으므로 추가적인 확인을 진행함
             */
            if (authentication == null || authentication is AnonymousAuthenticationToken) {
                throw AuthenticationNotValidException("401-2", "로그인이 필요합니다.")
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

    // 위치 등록
    @Transactional
    fun registerLocation(user: User, locationRequest: LocationRequest): UserDto {
        val optionalUser = userRepository.findByUsername(user.username)
            .orElseThrow{UserNotFoundException("404","유저를 찾을 수 없습니다")}

        return optionalUser.apply {
            latitude = locationRequest.latitude
            longitude = locationRequest.longitude
        } . let {
            userRepository.save(optionalUser)
        } . run {
            fromEntity(optionalUser)
        }
    }


    // 내 프로필 수정
    @Transactional
    fun updateMyProfile(user: User, updateRequest: UserUpdateRequest): UserDto {
        // 닉네임 변경

        val updateNickname = updateRequest.nickname
        if (updateNickname != null && updateNickname != user.nickname) {
            if (userRepository.existsByNickname(updateNickname)) {
                throw ServiceException("400-NICKNAME-ALREADY-EXISTS", "이미 사용중인 닉네임입니다.")
            }
            user.nickname = updateNickname
        }

        // 주소 변경
        if (updateRequest.address != null) {
            user.address = updateRequest.address
        }

        // 프로필 이미지 변경
        if (updateRequest.profileUrl != null) {
            user.profileUrl = updateRequest.profileUrl
        }

        // 이메일 변경 시 중복 체크
        val updateEmail = updateRequest.email
        if (updateEmail != null && updateEmail != user.email) {
            // 이메일 중복 체크 & 인증된 이메일인지 검증 및 예외처리
            userValidator.emailVerified(updateEmail)
            user.email = updateRequest.email
        }

        return fromEntity(user)
    }

    // 회원 탈퇴
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

    fun deleteAuthenticationCode(email: String) {
        emailService.deleteAuthenticationCode(email)
    }

    /**
     * 사용자가 입력한 인증코드를 검증하여 일치할 경우 해당 이메일을 verified로 저장
     *
     * @param email 검증할 이메일
     * @param code  사용자가 입력한 인증 코드
     * @throws ServiceException 인증코드가 일치하지 않는 경우
     */
    fun verifyAuthenticationCode(email: String, code: String) {
        emailService.getAuthenticationCode(email)
            ?.takeIf { emailService.verifyAuthenticationCode(code, it) }
            ?.also { emailService.saveAuthenticationCode(email, "verified") }
            ?: throw ServiceException("400-1", "인증코드가 틀렸습니다.")
    }

    // 관리자 유저 한명 반환
    val adminUsers: User
        get() = userRepository.findAllByRole(Role.ADMIN)
            .firstOrNull() ?: throw UserNotFoundException("404", "관리자 유저가 없습니다.")
}