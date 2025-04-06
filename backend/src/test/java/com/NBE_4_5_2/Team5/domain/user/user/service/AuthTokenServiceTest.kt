package com.NBE_4_5_2.Team5.domain.user.user.service

import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.NBE_4_5_2.Team5.global.standard.util.Ut
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@BaseTestConfig
class AuthTokenServiceTest {

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Autowired
    private lateinit var userService: UserService

    @Value("\${custom.jwt.secret-key}")
    private lateinit var keyString: String

    @Test
    @DisplayName("jwt : accessToken : user1 accessToken 생성 성공")
    fun accessToken() {
        // Given
        val user = userService.getUserByUsername("user1").orElseThrow()

        // When
        val accessToken = authTokenService.generateAccessToken(user)

        // Then
        Assertions.assertThat(accessToken).isNotBlank()
        println("accessToken = $accessToken")
    }

    @Test
    @DisplayName("jwt : 토큰 유효성 체크")
    fun checkValid() {
        // Given
        val user = userService.getUserByUsername("user1").orElseThrow()
        val accessToken = authTokenService.generateAccessToken(user)

        // When
        val isValid = Ut.Jwt.isValidToken(keyString, accessToken)
        val parsedPayload = authTokenService.getPayload(accessToken)

        // Then
        Assertions.assertThat(isValid).isTrue()
        Assertions.assertThat(parsedPayload).containsAllEntriesOf(
            mapOf("id" to user.id, "username" to user.username)
        )
    }
}
