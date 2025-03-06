package com.NBE_4_5_2.Team5.domain.user.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserService userService;

	private User loginedUser;
	private String token;

	@BeforeEach
	void login() {
		loginedUser = userService.findByUsername("user1").get();
		token = userService.getAuthToken(loginedUser);
	}

	private void checkUser(ResultActions resultActions, User user) throws Exception {
		resultActions.andExpect(jsonPath("$.data").exists())
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

	private ResultActions signupRequest(String username, String password, String email, String nickname, String address,
		String profileUrl) throws Exception {
		return mvc.perform(post("/api/users/signup").content("""
				{
				  "username": "%s",
				  "password": "%s",
				  "email": "%s",
				  "nickname": "%s",
				  "address": "%s",
				  "profileUrl": "%s"
				}
				""".formatted(username, password, email, nickname, address, profileUrl).stripIndent())
			.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))).andDo(print());
	}

	@Test
	@DisplayName("회원 가입 - 성공")
	void signup1() throws Exception {

		String username = "userNew";
		String password = "new1234@";
		String email = "new@naver.com";
		String nickname = "무명";
		String address = "서울시 강남구";
		String profileUrl = "default_profile.png";

		ResultActions resultActions = signupRequest(username, password, email, nickname, address, profileUrl);

		User user = userService.findByUsername(username).get();
		assertThat(user.getNickname()).isEqualTo(nickname);
		assertThat(user.getId()).startsWith("user-");

		resultActions.andExpect(status().isCreated())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(jsonPath("$.code").value("201-1"))
			.andExpect(jsonPath("$.message").value("회원 가입이 완료되었습니다."));

		checkUser(resultActions, user);

	}

	@Test
	@DisplayName("회원 가입 - 실패 - username 중복")
	void signup2() throws Exception {

		String username = "user1";
		String password = "new1234@";
		String email = "new@naver.com";
		String nickname = "무명";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = signupRequest(username, password, email, nickname, address, profileUrl);

		resultActions.andExpect(status().isConflict())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(jsonPath("$.code").value("409-1"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 아이디입니다."));

	}

	@Test
	@DisplayName("회원 가입 - 실패 - email 중복")
	void signup3() throws Exception {

		String username = "user4";
		String password = "new1234@";
		String email = "user1@gmail.com";
		String nickname = "무명";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = signupRequest(username, password, email, nickname, address, profileUrl);

		resultActions.andExpect(status().isConflict())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(jsonPath("$.code").value("409-2"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 이메일입니다."));

	}

	@Test
	@DisplayName("회원 가입 - 실패 - nickname 중복")
	void signup4() throws Exception {

		String username = "user4";
		String password = "new1234@";
		String email = "user4@gmail.com";
		String nickname = "user1";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = signupRequest(username, password, email, nickname, address, profileUrl);

		resultActions.andExpect(status().isConflict())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(jsonPath("$.code").value("409-3"))
			.andExpect(jsonPath("$.message").value("이미 사용중인 닉네임입니다."));

	}

	@Test
	@DisplayName("회원 가입 - 실패 - 필수 입력 데이터 누락")
	void signup5() throws Exception {

		String username = "";
		String password = "";
		String email = "";
		String nickname = "";
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = signupRequest(username, password, email, nickname, address, profileUrl);

		resultActions.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
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
	void signup6() throws Exception {

		String username = "wrong id"; // 공백 포함
		String password = "wrongpassword"; // 특수문자 미포함
		String email = "wrongemail"; // 이메일 형식 미준수
		String nickname = "i"; // 1자 미만
		String address = "서울시 강남구";
		String profileUrl = "https://example.com/default_profile.png";

		ResultActions resultActions = signupRequest(username, password, email, nickname, address, profileUrl);

		resultActions.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("""
				email : 올바른 이메일 형식이 아닙니다.
				nickname : 닉네임은 2~20자 사이여야 합니다.
				password : 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.
				username : 아이디는 영문과 숫자만 사용할 수 있습니다.
				""".stripIndent().stripTrailing()));
	}

	private ResultActions loginRequest(String username, String password) throws Exception {
		return mvc.perform(post("/api/users/login").content("""
				{
				  "username": "%s",
				  "password": "%s"
				}
				""".formatted(username, password).stripIndent())
			.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))).andDo(print());
	}

	@Test
	@DisplayName("로그인 - 성공")
	void login1() throws Exception {

		String username = "user1";
		String password = "user11234@";

		ResultActions resultActions = loginRequest(username, password);
		User user = userService.findByUsername(username).get();

		resultActions.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("%s님 환영합니다.".formatted(user.getNickname())))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andExpect(jsonPath("$.data.refreshToken").value(user.getRefreshToken()))
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

		resultActions.andExpect(mvcResult -> {
			Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");

			assertThat(refreshToken).isNotNull();
			assertThat(refreshToken.getName()).isEqualTo("refreshToken");
			assertThat(refreshToken.getValue()).isNotBlank();
			assertThat(refreshToken.getDomain()).isEqualTo("localhost");
			assertThat(refreshToken.getPath()).isEqualTo("/");
			assertThat(refreshToken.isHttpOnly()).isTrue();
			assertThat(refreshToken.getSecure()).isTrue();

			Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");

			assertThat(accessToken).isNotNull();
			assertThat(accessToken.getName()).isEqualTo("accessToken");
			assertThat(accessToken.getValue()).isNotBlank();
			assertThat(accessToken.getDomain()).isEqualTo("localhost");
			assertThat(accessToken.getPath()).isEqualTo("/");
			assertThat(accessToken.isHttpOnly()).isTrue();
			assertThat(accessToken.getSecure()).isTrue();

		});
	}

	@Test
	@DisplayName("로그인 - 실패 - 비밀번호 틀림")
	void login2() throws Exception {

		String username = "user1";
		String password = "1111";

		ResultActions resultActions = loginRequest(username, password);

		resultActions.andExpect(status().isUnauthorized())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(jsonPath("$.code").value("401-2"))
			.andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));

	}

	@Test
	@DisplayName("로그인 - 실패 - 존재하지 않는 username")
	void login3() throws Exception {

		String username = "stranger";
		String password = "user11234@";

		ResultActions resultActions = loginRequest(username, password);

		resultActions.andExpect(status().isUnauthorized())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("잘못된 아이디입니다."));

	}

	@Test
	@DisplayName("로그인 - 실패 - username 누락")
	void login4() throws Exception {

		String username = "";
		String password = "stranger";

		ResultActions resultActions = loginRequest(username, password);

		resultActions.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("username : 아이디는 필수 입력값입니다."));

	}

	@Test
	@DisplayName("로그인 - 실패 - password 누락")
	void login5() throws Exception {

		String username = "stranger";
		String password = "";

		ResultActions resultActions = loginRequest(username, password);

		resultActions.andExpect(status().isBadRequest())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("password : 비밀번호는 필수 입력값입니다."));

	}

	@Test
	@DisplayName("로그아웃")
	void logout() throws Exception {
		ResultActions resultActions = mvc.perform(post("/api/users/logout").header("Authorization", "Bearer " + token));

		resultActions.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("logout"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));

		resultActions.andExpect(mvcResult -> {
			Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");
			assertThat(refreshToken).isNotNull();
			assertThat(refreshToken.getMaxAge()).isZero();

			Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");
			assertThat(accessToken).isNotNull();
			assertThat(accessToken.getMaxAge()).isZero();
		});

	}

	private ResultActions meRequest(String token) throws Exception {
		return mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token)

		).andDo(print());
	}

	@Test
	@DisplayName("내 정보 조회 - 성공")
	void me1() throws Exception {

		String refreshToken = loginedUser.getRefreshToken();
		String token = userService.getAuthToken(loginedUser);

		ResultActions resultActions = meRequest(token);

		resultActions.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);

	}

	@Test
	@DisplayName("내 정보 조회 - 실패 - 잘못된 refreshToken")
	void me2() throws Exception {

		String refreshToken = "";

		ResultActions resultActions = meRequest(refreshToken);

		resultActions.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-1"))
			.andExpect(jsonPath("$.message").value("잘못된 인증키입니다."));

	}

	@Test
	@DisplayName("내 정보 조회 - 만료된 accessToken 사용")
	void me3() throws Exception {

		String refreshToken = loginedUser.getRefreshToken();
		String expiredToken = refreshToken
			+ " eyJhbGciOiJIUzUxMiJ9.eyJpZCI6InVzZXItNjVhYTMxMDUtMzYyNi00NzZlLThjMzgtZmU0OGVjNGQyMDNjIiwidXNlcm5hbWUiOiJ1c2VyMSIsImlhdCI6MTc0MDk5MTc2NCwiZXhwIjoxNzQwOTkxNzY5fQ.UypcRIORV4MyzY53-2W94z3jxP5VLbs5NGWOjJDZZ-O5yLxTHxhzDbU9LTNfcblQXaZAiypGU0m3EDq_RjHlsQ";

		ResultActions resultActions = meRequest(expiredToken);

		resultActions.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("me"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

		checkUser(resultActions, loginedUser);

	}

	private ResultActions refreshRequest(String refreshToken) throws Exception {
		return mvc.perform(post("/api/users/refresh").content("""
				{
				  "refreshToken": "%s"
				}
				""".formatted(refreshToken).stripIndent())
			.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))).andDo(print());
	}

	@Test
	@DisplayName("토큰 재발급 - 성공")
	void refresh1() throws Exception {

		String token = loginedUser.getRefreshToken();

		ResultActions resultActions = refreshRequest(token);

		resultActions.andExpect(status().isOk())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("refresh"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("AccessToken이 재발급되었습니다."))
			.andExpect(jsonPath("$.data").exists());

		resultActions.andExpect(mvcResult -> {
			Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");

			assertThat(refreshToken).isNotNull();
			assertThat(refreshToken.getName()).isEqualTo("refreshToken");
			assertThat(refreshToken.getValue()).isNotBlank();
			assertThat(refreshToken.getDomain()).isEqualTo("localhost");
			assertThat(refreshToken.getPath()).isEqualTo("/");
			assertThat(refreshToken.isHttpOnly()).isTrue();
			assertThat(refreshToken.getSecure()).isTrue();

			Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");

			assertThat(accessToken).isNotNull();
			assertThat(accessToken.getName()).isEqualTo("accessToken");
			assertThat(accessToken.getValue()).isNotBlank();
			assertThat(accessToken.getDomain()).isEqualTo("localhost");
			assertThat(accessToken.getPath()).isEqualTo("/");
			assertThat(accessToken.isHttpOnly()).isTrue();
			assertThat(accessToken.getSecure()).isTrue();

		});
	}

	@Test
	@DisplayName("토큰 재발급 - 실패 - 요청 body 누락")
	void refresh2() throws Exception {

		ResultActions resultActions = mvc.perform(
				post("/api/users/refresh").contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
			.andDo(print());

		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("refreshToken을 입력해주세요."));
	}

	@Test
	@DisplayName("토큰 재발급 - 실패 - refreshToken이 빈 문자열")
	void refresh3() throws Exception {
		String token = " ";
		ResultActions resultActions = refreshRequest(token);

		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("400-1"))
			.andExpect(jsonPath("$.message").value("refreshToken : refreshToken을 입력해주세요."));
	}

	@Test
	@DisplayName("토큰 재발급 - 실패 - 존재하지 않는 refreshToken")
	void refresh4() throws Exception {
		String fakeRefreshToken = "invalid_refresh_token";

		ResultActions resultActions = refreshRequest(fakeRefreshToken);

		resultActions.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("401-2"))
			.andExpect(jsonPath("$.message").value("유효하지 않은 RefreshToken입니다."));
	}

}