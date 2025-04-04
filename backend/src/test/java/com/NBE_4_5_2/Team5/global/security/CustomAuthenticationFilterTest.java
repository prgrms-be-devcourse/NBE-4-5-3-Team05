package com.NBE_4_5_2.Team5.global.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
class CustomAuthenticationFilterTest  {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserService userService;

	private User loginedUser;
	private String validAccessToken;
	private String validRefreshToken;

	@BeforeEach
	void setUp() {
		loginedUser = userService.getUserByUsername("user1").get();

		AuthToken authToken = userService.generateAuthtoken(loginedUser);
		validRefreshToken = authToken.refreshToken();
		validAccessToken = authToken.accessToken();
	}

	@Test
	@DisplayName("인증 - 실패 - 인증정보가 없고 토큰도 없음")
	void test1() throws Exception {

		ResultActions resultActions = mvc
			.perform(get("/api/users/me"))
			.andDo(print());

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	@DisplayName("인증 - 실패 - 익명 사용자 요청")
	void test2() throws Exception {

		SecurityContextHolder.getContext().setAuthentication( // 익명 사용자로 SecurityContext 설정
			new AnonymousAuthenticationToken(
				"anonymous", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
			)
		);

		ResultActions resultActions = mvc
			.perform(get("/api/users/me"))
			.andDo(print());

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	/**
	 * 예외 테스트 코드 (test3 ~ test8)
	 * <p>
	 * GlobalExceptionHandler에서 직접 처리하지 않은 예외 상황이 발생했을 때,
	 * Spring Boot의 기본 오류 처리 (`/error`)를 통해 정상적으로 응답이 반환되는지 검증하는 테스트
	 */
	@Test
	@DisplayName("예외 - 인증 o - 존재하지 않는 URL 요청 시 404 반환")
	void test3() throws Exception {
		mvc.perform(get("/api/non-existent-endpoint")
				.cookie(new Cookie("accessToken", validAccessToken))
				.cookie(new Cookie("refreshToken", validRefreshToken)))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("예외 - 인증 x - 존재하지 않는 URL 요청 시 404 반환")
	void test4() throws Exception {
		mvc.perform(get("/api/non-existent-endpoint"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("예외 - 인증 o - 잘못된 요청 시 400 반환")
	void test5() {
		Assertions.assertThatThrownBy(() ->
				mvc.perform(get("/api/test/bad-request")
						.cookie(new Cookie("accessToken", validAccessToken))
						.cookie(new Cookie("refreshToken", validRefreshToken)))
					.andDo(print())
			).isInstanceOf(ServletException.class)
			.cause()
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("예외 - 인증 x - 잘못된 요청 시 400 반환")
	void test6() {
		Assertions.assertThatThrownBy(() ->
				mvc.perform(get("/api/test/bad-request"))
					.andDo(print())
			).isInstanceOf(ServletException.class)
			.cause()
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("예외 - 인증 o - api 접속 중 NullPointerException 발생")
	void test7() {
		Assertions.assertThatThrownBy(() ->
				mvc.perform(get("/api/test/nullPointer-error")
						.cookie(new Cookie("accessToken", validAccessToken))
						.cookie(new Cookie("refreshToken", validRefreshToken)))
					.andDo(print())
			).isInstanceOf(ServletException.class)
			.cause()
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("예외 - 인증 x - api 접속 중 NullPointerException 발생")
	void test8() {
		Assertions.assertThatThrownBy(() ->
				mvc.perform(get("/api/test/nullPointer-error"))
					.andDo(print())
			).isInstanceOf(ServletException.class)
			.cause()
			.isInstanceOf(NullPointerException.class);
	}

}
