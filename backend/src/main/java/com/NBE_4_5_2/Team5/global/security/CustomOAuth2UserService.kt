package com.NBE_4_5_2.Team5.global.security

import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserValidator
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CustomOAuth2UserService(
    private val userService: UserService,
    private val userValidator: UserValidator,
    private val emailService: EmailService
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        // 1) 기본 OAuth2User 정보 로드
        val oAuth2User = super.loadUser(userRequest)

        // 2) 프로바이더, OAuth ID, 속성 꺼내기
        val oauthId      = oAuth2User.name
        val providerType = userRequest.clientRegistration.registrationId
        val props        = (oAuth2User.attributes["properties"] as? Map<String, Any>) ?: emptyMap()

        // 3) null-safe 하게 nickname/profileImage 추출
        val nickname     = props["nickname"]     as? String ?: ""
        val profileImage = props["profile_image"] as? String ?: ""
        val username     = "${providerType}__${oauthId}"

        // 4) 있으면 닉네임 업데이트, 없으면 신규 가입
        val user: User = userService.getUserByUsername(username)
            .map { existing ->
                existing.apply { update(nickname) }
            }
            .orElseGet {
                // 랜덤 이메일 생성 후 검증 코드 저장
                val randomEmail = "${UUID.randomUUID()}@kakao.com"
                emailService.saveVerificationCode(randomEmail, "verified")
                // 유저 생성
                userService.createUser(
                    username     = username,
                    password     = "",
                    email        = randomEmail,
                    nickname     = nickname,
                    address  = "",
                    profileUrl = profileImage
                )
            }

        // 5) SecurityUser 로 래핑해서 반환
        return SecurityUser(user)
    }
}
