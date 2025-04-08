package com.NBE_4_5_2.Team5.global.security

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * CustomAuthenticationFilterTest 클래스는 Spring Security의 인증 및 예외 상황을 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
class CustomAuthenticationFilterTest {

    @Autowired
    private lateinit var mvc: MockMvc                             // 웹 MVC 모킹

    @Autowired
    private lateinit var userService: UserService                 // 사용자 조회 서비스

    private lateinit var loginedUser: User                          // 로그인된 사용자
    private lateinit var validAccessToken: String                   // 유효한 액세스 토큰
    private lateinit var validRefreshToken: String                  // 유효한 리프레시 토큰

    @BeforeEach
    fun setUp() {
        // Given: "user1" 사용자를 조회 (존재하지 않으면 예외 발생)
        loginedUser = userService.getUserByUsername("user1").orElseThrow()
        // When: 사용자에 대한 인증 토큰 생성
        val authToken: AuthToken = userService.generateAuthtoken(loginedUser)
        // Then: 유효한 토큰들 설정
        validRefreshToken = authToken.refreshToken
        validAccessToken = authToken.accessToken
    }

    /**
     * 인증 정보 및 토큰이 없는 경우 /api/users/me 요청 시 401 Unauthorized 응답을 검증합니다.
     */
    @Test
    @DisplayName("인증 - 실패 - 인증정보가 없고 토큰도 없음")
    fun test1() {
        // When: 인증 정보 없이 /api/users/me 엔드포인트에 GET 요청 수행
        val resultActions = mvc.perform(get("/api/users/me")).andDo(print())
        // Then: 401 Unauthorized 응답과 함께 오류 코드 및 메시지가 반환됨
        resultActions.andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }

    /**
     * 익명 사용자 요청 시 /api/users/me 요청에 대해 401 Unauthorized 응답을 검증합니다.
     */
    @Test
    @DisplayName("인증 - 실패 - 익명 사용자 요청")
    fun test2() {
        // Given: 익명 사용자로 SecurityContext 설정
        SecurityContextHolder.getContext().authentication =
            AnonymousAuthenticationToken(
                "anonymous", "anonymousUser", listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            )
        // When: 인증 정보 없이 /api/users/me 엔드포인트에 GET 요청 수행
        val resultActions = mvc.perform(get("/api/users/me")).andDo(print())
        // Then: 401 Unauthorized 응답과 함께 오류 코드 및 메시지가 반환됨
        resultActions.andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }

    /**
     * 인증된 요청 시 존재하지 않는 URL 요청에 대해 404 Not Found 응답을 검증합니다.
     */
    @Test
    @DisplayName("예외 - 인증 o - 존재하지 않는 URL 요청 시 404 반환")
    fun test3() {
        // When: 유효한 토큰을 쿠키에 담아 존재하지 않는 엔드포인트에 GET 요청 수행
        mvc.perform(get("/api/non-existent-endpoint")
            .cookie(Cookie("accessToken", validAccessToken))
            .cookie(Cookie("refreshToken", validRefreshToken)))
            .andDo(print())
            .andExpect(status().isNotFound())
    }

    /**
     * 인증 정보 없이 존재하지 않는 URL 요청에 대해 404 Not Found 응답을 검증합니다.
     */
    @Test
    @DisplayName("예외 - 인증 x - 존재하지 않는 URL 요청 시 404 반환")
    fun test4() {
        // When: 인증 토큰 없이 존재하지 않는 엔드포인트에 GET 요청 수행
        mvc.perform(get("/api/non-existent-endpoint"))
            .andDo(print())
            .andExpect(status().isNotFound())
    }

    /**
     * 인증된 요청 시 잘못된 요청으로 인해 400 Bad Request 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("예외 - 인증 o - 잘못된 요청 시 400 반환")
    fun test5() {
        // When & Then: 유효한 토큰을 포함한 상태에서 /api/test/bad-request 요청 시 ServletException 발생, 원인은 IllegalArgumentException
        assertThatThrownBy {
            mvc.perform(get("/api/test/bad-request")
                .cookie(Cookie("accessToken", validAccessToken))
                .cookie(Cookie("refreshToken", validRefreshToken)))
                .andDo(print())
        }.isInstanceOf(ServletException::class.java)
            .hasCauseInstanceOf(IllegalArgumentException::class.java)
    }

    /**
     * 인증 정보 없이 잘못된 요청 시 400 Bad Request 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("예외 - 인증 x - 잘못된 요청 시 400 반환")
    fun test6() {
        // When & Then: 인증 토큰 없이 /api/test/bad-request 요청 시 ServletException 발생, 원인은 IllegalArgumentException
        assertThatThrownBy {
            mvc.perform(get("/api/test/bad-request"))
                .andDo(print())
        }.isInstanceOf(ServletException::class.java)
            .hasCauseInstanceOf(IllegalArgumentException::class.java)
    }

    /**
     * 인증된 요청 시 API 접속 중 NullPointerException 발생 시, 해당 예외가 ServletException의 원인이 되는지 검증합니다.
     */
    @Test
    @DisplayName("예외 - 인증 o - api 접속 중 NullPointerException 발생")
    fun test7() {
        // When & Then: 유효한 토큰을 포함한 상태에서 /api/test/nullPointer-error 요청 시 ServletException 발생, 원인은 NullPointerException
        assertThatThrownBy {
            mvc.perform(get("/api/test/nullPointer-error")
                .cookie(Cookie("accessToken", validAccessToken))
                .cookie(Cookie("refreshToken", validRefreshToken)))
                .andDo(print())
        }.isInstanceOf(ServletException::class.java)
            .hasCauseInstanceOf(NullPointerException::class.java)
    }

    /**
     * 인증 정보 없이 API 접속 중 NullPointerException 발생 시, 해당 예외가 ServletException의 원인이 되는지 검증합니다.
     */
    @Test
    @DisplayName("예외 - 인증 x - api 접속 중 NullPointerException 발생")
    fun test8() {
        // When & Then: 인증 토큰 없이 /api/test/nullPointer-error 요청 시 ServletException 발생, 원인은 NullPointerException
        assertThatThrownBy {
            mvc.perform(get("/api/test/nullPointer-error"))
                .andDo(print())
        }.isInstanceOf(ServletException::class.java)
            .hasCauseInstanceOf(NullPointerException::class.java)
    }
}
