package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.NBE_4_5_2.Team5.global.standard.util.Ut
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

/**
 * AuthTokenServiceTest 클래스는 JWT 인증 토큰의 생성 및 유효성 검증 동작을 테스트합니다.
 */
@SpringBootTest
@BaseTestConfig
class AuthTokenServiceTest {

    @Autowired
    private lateinit var authTokenService: AuthTokenService // 인증 토큰 생성 및 검증 서비스

    @Autowired
    private lateinit var userService: UserService // 사용자 조회 서비스

    @Value("\${custom.jwt.secret-key}")
    private lateinit var keyString: String // JWT 토큰 생성에 사용되는 시크릿 키

    /**
     * "user1" 사용자의 accessToken을 생성하고, 토큰이 유효하게 생성되었는지 테스트합니다.
     */
    @Test
    @DisplayName("jwt : accessToken : user1 accessToken 생성 성공")
    fun accessToken() {
        // Given: "user1" 사용자를 조회 (존재하지 않으면 예외 발생)
        val user = userService.getUserByUsername("user1").orElseThrow()

        // When: "user1"에 대해 JWT accessToken 생성
        val accessToken = authTokenService.generateAccessToken(user)

        // Then: 생성된 accessToken이 비어있지 않아야 함 (유효한 토큰)
        Assertions.assertThat(accessToken).isNotBlank()
        println("accessToken = $accessToken")  // 생성된 토큰을 콘솔에 출력
    }

    /**
     * 생성된 JWT 토큰의 유효성을 검증하고, 토큰 페이로드에 사용자의 id와 username이 포함되어 있는지 테스트합니다.
     */
    @Test
    @DisplayName("jwt : 토큰 유효성 체크")
    fun checkValid() {
        // Given: "user1" 사용자를 조회 및 accessToken 생성
        val user = userService.getUserByUsername("user1").orElseThrow()
        val accessToken = authTokenService.generateAccessToken(user)

        // When: 토큰 유효성 검사 및 페이로드 추출
        val isValid = Ut.Jwt.isValidToken(keyString, accessToken)
        val parsedPayload = authTokenService.getPayload(accessToken)

        // Then: 토큰이 유효하며, 페이로드에 해당 사용자의 id와 username이 포함되어 있어야 함
        Assertions.assertThat(isValid).isTrue()
        Assertions.assertThat(parsedPayload)
            .containsAllEntriesOf(mapOf("id" to user.id, "username" to user.username))
    }
}
