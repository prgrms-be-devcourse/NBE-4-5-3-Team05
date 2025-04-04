package com.NBE_4_5_2.Team5.domain.user.user.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@BaseTestConfig
@Order(100)
class UserControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserService userService;

	@Autowired
	EmailService emailService;

	private User loginedUser;

	private String validToken;
	private String validAccessToken;
	private String validRefreshToken;
	private String expiredAccessToken = "expiredAccessToken";
	private String invalidRefreshToken = "invalidRefreshToken";

	@BeforeEach
	void setUp() {
		loginedUser = userService.getUserByUsername("user1").orElseThrow();

		AuthToken authToken = userService.generateAuthtoken(loginedUser);
		validRefreshToken = authToken.refreshToken();
		validAccessToken = authToken.accessToken();
		validToken = validRefreshToken + " " + validAccessToken;
	}

	private void checkUser(ResultActions resultActions, User user) throws Exception {
		resultActions
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").value(user.getId()))
			.andExpect(jsonPath("$.data.username").value(user.getUsername()))
			.andExpect(jsonPath("$.data.email").value(user.getEmail()))
			.andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
			.andExpect(jsonPath("$.data.address").value(user.getAddress()))
			.andExpect(jsonPath("$.data.profileUrl").value(user.getProfileUrl()))
			.andExpect(jsonPath("$.data.role").value(user.getRole().toString()))
			.andExpect(jsonPath("$.data.createdAt").value(
				matchesPattern(user.getCreatedAt().toString().replaceAll("0+$", "") + ".*")))
			.andExpect(jsonPath("$.data.modifiedAt").value(
				matchesPattern(user.getModifiedAt().toString().replaceAll("0+$", "") + ".*")));
	}

	private ResultActions createUserRequest(String username, String password, String email, String nickname,
		String address, String profileUrl) throws Exception {
		return mvc
			.perform(
				post("/api/users/signup")
					.content("""
						{
						  "username": "%s",
						  "password": "%s",
						  "email": "%s",
						  "nickname": "%s",
						  "address": "%s",
						  "profileUrl": "%s"
						}
						"""
						.formatted(username, password, email, nickname, address, profileUrl)
						.stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());
	}

	@Test
	@DisplayName("회원 가입 - 성공")
	void createUser1() throws Exception {

		String username = "userNew";
		String password = "new1234@";
		String email = "new@naver.com";
		String nickname = "무명";
		String address = "서울시 강남구";
		String profileUrl = "default_profile.png";

		// 이메일 인증이 통과되었다고 가정
		emailService.saveVerificationCode(email, "verified");

		ResultActions resultActions = createUserRequest(username, password, email, nickname, address, profileUrl);

		User user = userService.getUserByUsername(username).get();
		assertThat(user.getNickname()).isEqualTo(nickname);
		assertThat(user.getId()).startsWith("user-");

		resultActions
			.andExpect(status().isCreated())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("201-1"))
			.andExpect(jsonPath("$.message").value("회원 가입이 완료되었습니다."));

		checkUser(resultActions, user);

	}

	@Test
	@DisplayName("회원 가입 - 실패 - username 중복")
	void createUser2() throws Exception {

		String username = "user1";
		String password = "new1234@";
		String email = "new2@naver.com";
		String nickname = "무명";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = createUserRequest(username, password, email, nickname, address, profileUrl);

		resultActions
			.andExpect(status().isConflict())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("409-1"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 아이디입니다."));

	}

	@Test
	@DisplayName("회원 가입 - 실패 - email 중복")
	void createUser3() throws Exception {

		String username = "user5";
		String password = "new1234@";
		String email = "user1@gmail.com";
		String nickname = "무명";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = createUserRequest(username, password, email, nickname, address, profileUrl);

		resultActions
			.andExpect(status().isConflict())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("409-2"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 이메일입니다."));

	}

	@Test
	@DisplayName("회원 가입 - 실패 - email 인증 안함")
	void createUser4() throws Exception {

		String username = "user12";
		String password = "new1234@";
		String email = "new@gmail.com";
		String nickname = "무명";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = createUserRequest(username, password, email, nickname, address, profileUrl);

		resultActions
			.andExpect(status().isConflict())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("409"))
			.andExpect(jsonPath("$.message").value("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요."));

	}

	@Test
	@DisplayName("회원 가입 - 실패 - nickname 중복")
	void createUser5() throws Exception {

		String username = "user5";
		String password = "new1234@";
		String email = "user5@gmail.com";
		String nickname = "user1";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = createUserRequest(username, password, email, nickname, address, profileUrl);

		resultActions
			.andExpect(status().isConflict())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("409-3"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 닉네임입니다."));

	}

	@Test
	@DisplayName("회원 가입 - 실패 - 필수 입력 데이터 누락")
	void createUser6() throws Exception {

		String username = "";
		String password = "";
		String email = "";
		String nickname = "";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = createUserRequest(username, password, email, nickname, address, profileUrl);

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("""
				email : 이메일은 필수 입력값입니다.
				nickname : 닉네임은 2~20자 사이여야 합니다.
				password : 비밀번호는 8~50자 사이여야 합니다.
				password : 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.
				username : 아이디는 4~20자 사이여야 합니다.
				username : 아이디는 영문과 숫자만 사용할 수 있습니다.
				""".stripIndent().stripTrailing()));
	}

	@Test
	@DisplayName("회원 가입 - 실패 - 잘못된 형식의 데이터 입력")
	void createUser7() throws Exception {

		String username = "wrong id"; // 공백 포함
		String password = "wrongpassword"; // 특수문자 미포함
		String email = "wrongemail"; // 이메일 형식 미준수
		String nickname = "i"; // 1자 미만
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = createUserRequest(username, password, email, nickname, address, profileUrl);

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("""
				email : 올바른 이메일 형식이 아닙니다.
				nickname : 닉네임은 2~20자 사이여야 합니다.
				password : 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.
				username : 아이디는 영문과 숫자만 사용할 수 있습니다.
				""".stripIndent().stripTrailing()));
	}

	@Test
	@DisplayName("회원 가입 - 실패 - 요청 body 누락")
	void createUser8() throws Exception {

		ResultActions resultActions = mvc
			.perform(
				post("/api/users/signup")
			)
			.andDo(print());

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("createUser"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("값을 입력해주세요."));
	}

	private ResultActions loginUserRequest(String username, String password) throws Exception {
		return mvc
			.perform(
				post("/api/users/login")
					.content("""
						{
						  "username": "%s",
						  "password": "%s"
						}
						"""
						.formatted(username, password)
						.stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 - 성공")
	void loginUser1() throws Exception {

		String username = "user2";
		String password = "user21234@";

		ResultActions resultActions = loginUserRequest(username, password);
		User user = userService.getUserByUsername(username).get();
		String refreshToken = userService.getRefreshTokenByUserId(user.getId());

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("loginUser"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("%s님 환영합니다.".formatted(user.getNickname())))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andExpect(jsonPath("$.data.refreshToken").value(refreshToken))
			.andExpect(jsonPath("$.data.item.id").value(user.getId()))
			.andExpect(jsonPath("$.data.item.username").value(user.getUsername()))
			.andExpect(jsonPath("$.data.item.email").value(user.getEmail()))
			.andExpect(jsonPath("$.data.item.nickname").value(user.getNickname()))
			.andExpect(jsonPath("$.data.item.address").value(user.getAddress()))
			.andExpect(jsonPath("$.data.item.profileUrl").value(user.getProfileUrl()))
			.andExpect(jsonPath("$.data.item.role").value(user.getRole().toString()))
			.andExpect(jsonPath("$.data.item.createdAt").value(
				matchesPattern(user.getCreatedAt().toString().replaceAll("0+$", "") + ".*")))
			.andExpect(jsonPath("$.data.item.modifiedAt").value(
				matchesPattern(user.getModifiedAt().toString().replaceAll("0+$", "") + ".*")));

		resultActions
			.andExpect(mvcResult -> {
				Cookie cookieRefreshToken = mvcResult.getResponse().getCookie("refreshToken");

				assertThat(cookieRefreshToken).isNotNull();
				assertThat(cookieRefreshToken.getName()).isEqualTo("refreshToken");
				assertThat(cookieRefreshToken.getValue()).isNotBlank();
				assertThat(cookieRefreshToken.getPath()).isEqualTo("/");
				assertThat(cookieRefreshToken.isHttpOnly()).isTrue();
				assertThat(cookieRefreshToken.getSecure()).isTrue();

				Cookie cookieAccessToken = mvcResult.getResponse().getCookie("accessToken");

				assertThat(cookieAccessToken).isNotNull();
				assertThat(cookieAccessToken.getName()).isEqualTo("accessToken");
				assertThat(cookieAccessToken.getValue()).isNotBlank();
				assertThat(cookieAccessToken.getPath()).isEqualTo("/");
				assertThat(cookieAccessToken.isHttpOnly()).isTrue();
				assertThat(cookieAccessToken.getSecure()).isTrue();

			});
	}

	@Test
	@DisplayName("로그인 - 실패 - 비밀번호 틀림")
	void loginUser2() throws Exception {

		String username = "user1";
		String password = "1111";

		ResultActions resultActions = loginUserRequest(username, password);

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("loginUser"))
			.andExpect(jsonPath("$.code").value("401-2"))
			.andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));

	}

	@Test
	@DisplayName("로그인 - 실패 - 존재하지 않는 username")
	void loginUser3() throws Exception {

		String username = "stranger";
		String password = "user11234@";

		ResultActions resultActions = loginUserRequest(username, password);

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("loginUser"))
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("잘못된 아이디입니다."));

	}

	@Test
	@DisplayName("로그인 - 실패 - username 누락")
	void loginUser4() throws Exception {

		String username = "";
		String password = "stranger";

		ResultActions resultActions = loginUserRequest(username, password);

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("loginUser"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("username : 아이디는 필수 입력값입니다."));

	}

	@Test
	@DisplayName("로그인 - 실패 - password 누락")
	void loginUser5() throws Exception {

		String username = "stranger";
		String password = "";

		ResultActions resultActions = loginUserRequest(username, password);

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("loginUser"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("password : 비밀번호는 필수 입력값입니다."));

	}

	@Test
	@DisplayName("로그인 - 실패 - 요청 body 누락")
	void loginUser6() throws Exception {

		ResultActions resultActions = mvc
			.perform(
				post("/api/users/login")
			)
			.andDo(print());

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("loginUser"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("값을 입력해주세요."));

	}

	@Test
	@DisplayName("로그아웃 - 성공 - 헤더 인증")
	void logoutUser1() throws Exception {
		ResultActions resultActions = mvc.perform(
			post("/api/users/logout")
				.header("Authorization", "Bearer " + validToken)
		);

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("logoutUser"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));

		resultActions.
			andExpect(
				mvcResult -> {
					Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");
					assertThat(refreshToken).isNotNull();
					assertThat(refreshToken.getMaxAge()).isLessThanOrEqualTo(0);
					;

					Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");
					assertThat(accessToken).isNotNull();
					assertThat(refreshToken.getMaxAge()).isLessThanOrEqualTo(0);
					;
				}
			);

	}

	@Test
	@DisplayName("로그아웃 - 성공 - 쿠키 인증")
	void logoutUser2() throws Exception {
		ResultActions resultActions = mvc.perform(
			post("/api/users/logout")
				.cookie(new Cookie("accessToken", validAccessToken))
				.cookie(new Cookie("refreshToken", validRefreshToken))
		);

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("logoutUser"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));

		resultActions.
			andExpect(
				mvcResult -> {
					Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");
					assertThat(refreshToken).isNotNull();
					assertThat(refreshToken.getMaxAge()).isLessThanOrEqualTo(0);

					Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");
					assertThat(accessToken).isNotNull();
					assertThat(refreshToken.getMaxAge()).isLessThanOrEqualTo(0);
				}
			);

	}

	@Test
	@DisplayName("내 정보 조회 - 성공 - 쿠키 인증 - 유효한 AccessToken + 유효한 RefreshToken")
	void me4() throws Exception {

		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.cookie(new Cookie("accessToken", validAccessToken))
					.cookie(new Cookie("refreshToken", validRefreshToken))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);
	}

	@Test
	@DisplayName("내 정보 조회 - 성공 - 쿠키 인증 - 만료된 AccessToken + 유효한 RefreshToken")
	void me5() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.cookie(new Cookie("accessToken", expiredAccessToken))
					.cookie(new Cookie("refreshToken", validRefreshToken))
			)
			.andDo(print());

		// refreshToken으로 accessToken 재발급 후 정상적으로 인증
		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);
	}

	@Test
	@DisplayName("내 정보 조회 - 성공 - 쿠키 인증 - 유효한 AccessToken + 만료된 RefreshToken")
	void me6() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.cookie(new Cookie("accessToken", validAccessToken))
					.cookie(new Cookie("refreshToken", invalidRefreshToken))
			)
			.andDo(print());

		// RefreshToken이 만료되었더라도 accessToken을 통한 정상적 인증
		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);
	}

	@Test
	@DisplayName("내 정보 조회 - 실패 - 쿠키 인증 - 만료된 AccessToken + 잘못된 RefreshToken")
	void me7() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.cookie(new Cookie("accessToken", expiredAccessToken))
					.cookie(new Cookie("refreshToken", invalidRefreshToken))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	@DisplayName("내 정보 조회 - 실패 - 쿠키 인증 - 잘못된 형식 (잘못된 값이 들어오거나 하나의 토큰만 존재)")
	void me8() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.cookie(new Cookie("refreshToken", validRefreshToken))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	@DisplayName("내 정보 조회 - 성공 - 헤더 인증 - 유효한 RefreshToken + 유효한 AccessToken")
	void me9() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.header("Authorization", "Bearer " + validToken)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);
	}

	@Test
	@DisplayName("내 정보 조회 - 성공 - 헤더 인증 - 만료된 AccessToken + 유효한 RefreshToken")
	void me10() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.header("Authorization", "Bearer " + validRefreshToken + " " + expiredAccessToken)
			)
			.andDo(print());

		// refreshToken을 통해 accessToken이 재발급되고 정상 인증됨
		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);
	}

	@Test
	@DisplayName("내 정보 조회 - 성공 - 헤더 인증 - 유효한 AccessToken + 만료된 RefreshToken")
	void me11() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.header("Authorization", "Bearer " + invalidRefreshToken + " " + validAccessToken)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);

	}

	@Test
	@DisplayName("내 정보 조회 - 실패 - 헤더 인증 - 만료된 AccessToken + 만료된 RefreshToken")
	void me12() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.header("Authorization", "Bearer " + invalidRefreshToken + " " + expiredAccessToken)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	@DisplayName("내 정보 조회 - 실패 - 헤더 인증 - 잘못된 형식 (잘못된 값이 들어오거나 하나의 토큰만 존재)")
	void me13() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/users/me")
					.header("Authorization", "Bearer " + validRefreshToken)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	private ResultActions refreshAccessTokenRequest(String refreshToken) throws Exception {

		return mvc
			.perform(
				post("/api/users/refresh")
					.content("""
						{
						  "refreshToken": "%s"
						}
						"""
						.formatted(refreshToken)
						.stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());
	}

	@Test
	@DisplayName("토큰 재발급 - 성공")
	void refreshAccessToken1() throws Exception {

		ResultActions resultActions = refreshAccessTokenRequest(validRefreshToken);

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("refreshAccessToken"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("AccessToken이 재발급되었습니다."))
			.andExpect(jsonPath("$.data").exists());

		resultActions
			.andExpect(mvcResult -> {

				Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");

				assertThat(accessToken).isNotNull();
				assertThat(accessToken.getName()).isEqualTo("accessToken");
				assertThat(accessToken.getValue()).isNotBlank();
				assertThat(accessToken.getPath()).isEqualTo("/");
				assertThat(accessToken.isHttpOnly()).isTrue();
				assertThat(accessToken.getSecure()).isTrue();

			});
	}

	@Test
	@DisplayName("토큰 재발급 - 실패 - 요청 body 누락")
	void refreshAccessToken2() throws Exception {

		ResultActions resultActions = mvc
			.perform(
				post("/api/users/refresh")
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("값을 입력해주세요."));
	}

	@Test
	@DisplayName("토큰 재발급 - 실패 - refreshToken이 빈 문자열")
	void refreshAccessToken3() throws Exception {
		String token = " ";
		ResultActions resultActions = refreshAccessTokenRequest(token);

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("refreshToken : refreshToken을 입력해주세요."));
	}

	@Test
	@DisplayName("토큰 재발급 - 실패 - 존재하지 않는 refreshToken")
	void refreshAccessToken4() throws Exception {
		String fakeRefreshToken = "invalid_refresh_token";

		ResultActions resultActions = refreshAccessTokenRequest(fakeRefreshToken);

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("유효하지 않은 RefreshToken입니다."));
	}

	@Test
	@DisplayName("내 정보 수정 - 성공")
	void updateProfile1() throws Exception {
		String newNickname = "새로운닉네임";
		String newAddress = "서울시 서초구";
		String newEmail = "newemail@example.com";

		// 이메일 인증이 통과되었다고 가정
		emailService.saveVerificationCode(newEmail, "verified");

		ResultActions resultActions = mvc.perform(
			put("/api/users/me")
				.header("Authorization", "Bearer " + validToken)
				.content("""
					{
					  "nickname": "%s",
					  "address": "%s",
					  "email": "%s"
					}
					""".formatted(newNickname, newAddress, newEmail))
				.contentType(MediaType.APPLICATION_JSON)
		);

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("사용자 정보가 성공적으로 수정되었습니다."));

		// 변경 사항 확인
		User updatedUser = userService.getUserByUsername(loginedUser.getUsername()).get();
		assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
		assertThat(updatedUser.getAddress()).isEqualTo(newAddress);
		assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
	}

	@Test
	@DisplayName("내 정보 수정 - 실패 - 이메일 중복")
	void updateProfile2() throws Exception {
		String duplicateEmail = "user2@gmail.com"; // 이미 존재하는 이메일

		ResultActions resultActions = mvc.perform(
			put("/api/users/me")
				.header("Authorization", "Bearer " + validToken)
				.content("""
					{
					  "email": "%s"
					}
					""".formatted(duplicateEmail))
				.contentType(MediaType.APPLICATION_JSON)
		);

		resultActions
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("409-2"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 이메일입니다."));
	}

	@Test
	@DisplayName("내 정보 수정 - 실패 - 이메일 인증 안함")
	void updateProfile3() throws Exception {
		String duplicateEmail = "invaild@gmail.com"; // 인증하지 않은 이메일

		ResultActions resultActions = mvc.perform(
			put("/api/users/me")
				.header("Authorization", "Bearer " + validToken)
				.content("""
					{
					  "email": "%s"
					}
					""".formatted(duplicateEmail))
				.contentType(MediaType.APPLICATION_JSON)
		);

		resultActions
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("409"))
			.andExpect(jsonPath("$.message").value("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요."));
	}

	@Test
	@DisplayName("내 정보 수정 - 실패 - 닉네임 중복")
	void updateProfile4() throws Exception {
		String duplicateNickname = "user3"; // 이미 존재하는 닉네임

		ResultActions resultActions = mvc.perform(
			put("/api/users/me")
				.header("Authorization", "Bearer " + validToken)
				.content("""
					{
					  "nickname": "%s"
					}
					""".formatted(duplicateNickname))
				.contentType(MediaType.APPLICATION_JSON)
		);

		resultActions
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("400-NICKNAME-ALREADY-EXISTS"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 닉네임입니다."));
	}

	@Test
	@DisplayName("회원 탈퇴 - 성공")
	void deleteProfile1() throws Exception {
		ResultActions resultActions = mvc.perform(
			delete("/api/users/me")
				.header("Authorization", "Bearer " + validToken)
		);

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("회원 탈퇴 성공"));

		// 회원이 실제로 삭제되었는지 확인
		boolean isDeleted = isUserDeleted(loginedUser.getUsername());
		assertThat(isDeleted).isTrue();
	}

	@Transactional
	boolean isUserDeleted(String username) {
		return userService.getUserByUsername(username).isEmpty();
	}

	@Test
	@DisplayName("회원 탈퇴 - 실패 (토큰 없음)")
	void deleteProfile2() throws Exception {
		ResultActions resultActions = mvc.perform(delete("/api/users/me"));

		resultActions
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	@DisplayName("회원 탈퇴 - 실패 (잘못된 토큰)")
	void deleteProfile3() throws Exception {
		ResultActions resultActions = mvc.perform(
			delete("/api/users/me")
				.header("Authorization", "Bearer wrong-token")
		);

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

}