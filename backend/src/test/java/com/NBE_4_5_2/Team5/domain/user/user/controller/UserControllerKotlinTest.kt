package com.NBE_4_5_2.Team5.domain.user.user.controller

import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.nio.charset.StandardCharsets

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@BaseTestConfig
@Order(100)
internal open class UserControllerKotlinTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var emailService: EmailService

    private lateinit var loginedUser: User

    private lateinit var validToken: String
    private lateinit var validAccessToken: String
    private lateinit var validRefreshToken: String
    private val expiredAccessToken = "expiredAccessToken"
    private val invalidRefreshToken = "invalidRefreshToken"

    @BeforeEach
    fun setUp() {
        // GIVEN: 기존 유저 "user1"를 가져와 로그인 상태로 준비
        loginedUser = userService.getUserByUsername("user1").orElseThrow()
        val authToken = userService.generateAuthtoken(loginedUser)
        validRefreshToken = authToken.refreshToken
        validAccessToken = authToken.accessToken
        validToken = "$validRefreshToken $validAccessToken"
    }

    // Helper: 응답에서 유저 정보를 검증 (THEN)
    @Throws(Exception::class)
    private fun checkUser(resultActions: ResultActions, user: User) {
        resultActions
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(user.id))
            .andExpect(jsonPath("$.data.username").value(user.username))
            .andExpect(jsonPath("$.data.email").value(user.email))
            .andExpect(jsonPath("$.data.nickname").value(user.nickname))
            .andExpect(jsonPath("$.data.address").value(user.address))
            .andExpect(jsonPath("$.data.profileUrl").value(user.profileUrl))
            .andExpect(jsonPath("$.data.role").value(user.role.toString()))
            .andExpect(
                jsonPath("$.data.createdAt").value(
                    Matchers.matchesPattern(user.createdAt.toString().replace("0+$".toRegex(), "") + ".*")
                )
            )
            .andExpect(
                jsonPath("$.data.modifiedAt").value(
                    Matchers.matchesPattern(user.modifiedAt.toString().replace("0+$".toRegex(), "") + ".*")
                )
            )
    }

    // Helper: 회원 가입 API 요청 (GIVEN)
    @Throws(Exception::class)
    private fun createUserRequest(
        username: String, password: String, email: String, nickname: String,
        address: String, profileUrl: String
    ): ResultActions {
        return mvc.perform(
            post("/api/users/signup")
                .content(
                    """
                    {
                      "username": "$username",
                      "password": "$password",
                      "email": "$email",
                      "nickname": "$nickname",
                      "address": "$address",
                      "profileUrl": "$profileUrl"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())
    }

    @Test
    @DisplayName("회원 가입 - 성공")
    @Throws(Exception::class)
    fun createUser1() {
        // GIVEN: 신규 회원 정보를 준비 (이메일 인증 완료 가정)
        val username = "userNew"
        val password = "new1234@"
        val email = "new@naver.com"
        val nickname = "무명"
        val address = "서울시 강남구"
        val profileUrl = "default_profile.png"
        emailService.saveVerificationCode(email, "verified")

        // WHEN: 회원 가입 API 호출
        val resultActions = createUserRequest(username, password, email, nickname, address, profileUrl)

        // THEN: 회원이 정상적으로 생성되었는지 DB와 응답을 통해 검증
        val user = userService.getUserByUsername(username).get()
        assertThat(user.nickname).isEqualTo(nickname)
        assertThat(user.id).startsWith("user-")
        resultActions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("201-1"))
            .andExpect(jsonPath("$.message").value("회원 가입이 완료되었습니다."))
        checkUser(resultActions, user)
    }

    @Test
    @DisplayName("회원 가입 - 실패 - username 중복")
    @Throws(Exception::class)
    fun createUser2() {
        // GIVEN: 이미 존재하는 username("user1") 사용
        val username = "user1"
        val password = "new1234@"
        val email = "new2@naver.com"
        val nickname = "무명"
        val address = "서울시 강남구"
        val profileUrl = "https://example.com/default_profile.png"

        // WHEN: 회원 가입 API 호출
        val resultActions = createUserRequest(username, password, email, nickname, address, profileUrl)

        // THEN: Conflict 응답 확인 (username 중복)
        resultActions
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("409-1"))
            .andExpect(jsonPath("$.message").value("이미 사용중인 아이디입니다."))
    }

    @Test
    @DisplayName("회원 가입 - 실패 - email 중복")
    @Throws(Exception::class)
    fun createUser3() {
        // GIVEN: 이미 사용 중인 email("user1@gmail.com") 사용
        val username = "user5"
        val password = "new1234@"
        val email = "user1@gmail.com"
        val nickname = "무명"
        val address = "서울시 강남구"
        val profileUrl = "https://example.com/default_profile.png"

        // WHEN: 회원 가입 API 호출
        val resultActions = createUserRequest(username, password, email, nickname, address, profileUrl)

        // THEN: Conflict 응답 확인 (email 중복)
        resultActions
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("409-2"))
            .andExpect(jsonPath("$.message").value("이미 사용중인 이메일입니다."))
    }

    @Test
    @DisplayName("회원 가입 - 실패 - email 인증 안함")
    @Throws(Exception::class)
    fun createUser4() {
        // GIVEN: 이메일 인증이 안 된 상태의 회원 가입 요청
        val username = "user12"
        val password = "new1234@"
        val email = "new@gmail.com"
        val nickname = "무명"
        val address = "서울시 강남구"
        val profileUrl = "https://example.com/default_profile.png"

        // WHEN: 회원 가입 API 호출
        val resultActions = createUserRequest(username, password, email, nickname, address, profileUrl)

        // THEN: Conflict 응답 확인 (이메일 인증 미완료)
        resultActions
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("409"))
            .andExpect(jsonPath("$.message").value("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요."))
    }

    @Test
    @DisplayName("회원 가입 - 실패 - nickname 중복")
    @Throws(Exception::class)
    fun createUser5() {
        // GIVEN: 이미 사용 중인 nickname("user1") 사용
        val username = "user5"
        val password = "new1234@"
        val email = "user5@gmail.com"
        val nickname = "user1"
        val address = "서울시 강남구"
        val profileUrl = "https://example.com/default_profile.png"

        // WHEN: 회원 가입 API 호출
        val resultActions = createUserRequest(username, password, email, nickname, address, profileUrl)

        // THEN: Conflict 응답 확인 (nickname 중복)
        resultActions
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("409-3"))
            .andExpect(jsonPath("$.message").value("이미 사용중인 닉네임입니다."))
    }

    @Test
    @DisplayName("회원 가입 - 실패 - 필수 입력 데이터 누락")
    @Throws(Exception::class)
    fun createUser6() {
        // GIVEN: 필수 입력 데이터가 누락된 회원 가입 요청
        val username = ""
        val password = ""
        val email = ""
        val nickname = ""
        val address = "서울시 강남구"
        val profileUrl = "https://example.com/default_profile.png"

        // WHEN: 회원 가입 API 호출
        val resultActions = createUserRequest(username, password, email, nickname, address, profileUrl)

        // THEN: BadRequest 응답 확인 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(
                jsonPath("$.message").value(
                    """
                    email : 이메일은 필수 입력값입니다.
                    nickname : 닉네임은 2~20자 사이여야 합니다.
                    password : 비밀번호는 8~50자 사이여야 합니다.
                    password : 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.
                    username : 아이디는 4~20자 사이여야 합니다.
                    username : 아이디는 영문과 숫자만 사용할 수 있습니다.
                    """.trimIndent().trimEnd()
                )
            )
    }

    @Test
    @DisplayName("회원 가입 - 실패 - 잘못된 형식의 데이터 입력")
    @Throws(Exception::class)
    fun createUser7() {
        // GIVEN: 형식에 맞지 않는 데이터로 회원 가입 요청
        val username = "wrong id" // 공백 포함
        val password = "wrongpassword" // 특수문자 미포함
        val email = "wrongemail" // 이메일 형식 미준수
        val nickname = "i" // 1자 미만
        val address = "서울시 강남구"
        val profileUrl = "https://example.com/default_profile.png"

        // WHEN: 회원 가입 API 호출
        val resultActions = createUserRequest(username, password, email, nickname, address, profileUrl)

        // THEN: BadRequest 응답 확인 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(
                jsonPath("$.message").value(
                    """
                    email : 올바른 이메일 형식이 아닙니다.
                    nickname : 닉네임은 2~20자 사이여야 합니다.
                    password : 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.
                    username : 아이디는 영문과 숫자만 사용할 수 있습니다.
                    """.trimIndent().trimEnd()
                )
            )
    }

    @Test
    @DisplayName("회원 가입 - 실패 - 요청 body 누락")
    @Throws(Exception::class)
    fun createUser8() {
        // WHEN: 요청 body가 없는 회원 가입 API 호출
        val resultActions = mvc.perform(post("/api/users/signup")).andDo(print())

        // THEN: BadRequest 응답 확인 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.message").value("값을 입력해주세요."))
    }

    // Helper: 로그인 API 호출하여 쿠키 반환 (GIVEN)
    @Throws(Exception::class)
    private fun loginUserRequest(username: String, password: String): ResultActions {
        return mvc.perform(
            post("/api/users/login")
                .content(
                    """
                    {
                      "username": "$username",
                      "password": "$password"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())
    }

    @Test
    @DisplayName("로그인 - 성공")
    @Throws(Exception::class)
    fun loginUser1() {
        // GIVEN: 로그인 정보를 준비 (user2)
        val username = "user2"
        val password = "user21234@"

        // WHEN: 로그인 API 호출
        val resultActions = loginUserRequest(username, password)
        val user = userService.getUserByUsername(username).get()
        val refreshToken = userService.getRefreshTokenByUserId(user.id)

        // THEN: 응답과 쿠키에 로그인 정보가 올바르게 포함되었는지 검증
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("${user.nickname}님 환영합니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").value(refreshToken))
            .andExpect(jsonPath("$.data.item.id").value(user.id))
            .andExpect(jsonPath("$.data.item.username").value(user.username))
            .andExpect(jsonPath("$.data.item.email").value(user.email))
            .andExpect(jsonPath("$.data.item.nickname").value(user.nickname))
            .andExpect(jsonPath("$.data.item.address").value(user.address))
            .andExpect(jsonPath("$.data.item.profileUrl").value(user.profileUrl))
            .andExpect(jsonPath("$.data.item.role").value(user.role.toString()))
            .andExpect(
                jsonPath("$.data.item.createdAt").value(
                    Matchers.matchesPattern(user.createdAt.toString().replace("0+$".toRegex(), "") + ".*")
                )
            )
            .andExpect(
                jsonPath("$.data.item.modifiedAt").value(
                    Matchers.matchesPattern(user.modifiedAt.toString().replace("0+$".toRegex(), "") + ".*")
                )
            )

        resultActions.andExpect { mvcResult: MvcResult ->
            val cookieRefreshToken = mvcResult.response.getCookie("refreshToken")
            assertThat(cookieRefreshToken).isNotNull
            assertThat(cookieRefreshToken.name).isEqualTo("refreshToken")
            assertThat(cookieRefreshToken.value).isNotBlank
            assertThat(cookieRefreshToken.path).isEqualTo("/")
            assertThat(cookieRefreshToken.isHttpOnly).isTrue
            assertThat(cookieRefreshToken.secure).isTrue

            val cookieAccessToken = mvcResult.response.getCookie("accessToken")
            assertThat(cookieAccessToken).isNotNull
            assertThat(cookieAccessToken.name).isEqualTo("accessToken")
            assertThat(cookieAccessToken.value).isNotBlank
            assertThat(cookieAccessToken.path).isEqualTo("/")
            assertThat(cookieAccessToken.isHttpOnly).isTrue
            assertThat(cookieAccessToken.secure).isTrue
        }
    }

    @Test
    @DisplayName("로그인 - 실패 - 비밀번호 틀림")
    @Throws(Exception::class)
    fun loginUser2() {
        // GIVEN: 올바르지 않은 비밀번호 제공
        val username = "user1"
        val password = "1111"

        // WHEN: 로그인 API 호출
        val resultActions = loginUserRequest(username, password)

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-2"))
            .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
    }

    @Test
    @DisplayName("로그인 - 실패 - 존재하지 않는 username")
    @Throws(Exception::class)
    fun loginUser3() {
        // GIVEN: 존재하지 않는 username 제공
        val username = "stranger"
        val password = "user11234@"

        // WHEN: 로그인 API 호출
        val resultActions = loginUserRequest(username, password)

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("잘못된 아이디입니다."))
    }

    @Test
    @DisplayName("로그인 - 실패 - username 누락")
    @Throws(Exception::class)
    fun loginUser4() {
        // GIVEN: username 누락
        val username = ""
        val password = "stranger"

        // WHEN: 로그인 API 호출
        val resultActions = loginUserRequest(username, password)

        // THEN: BadRequest 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.message").value("username : 아이디는 필수 입력값입니다."))
    }

    @Test
    @DisplayName("로그인 - 실패 - password 누락")
    @Throws(Exception::class)
    fun loginUser5() {
        // GIVEN: password 누락
        val username = "stranger"
        val password = ""

        // WHEN: 로그인 API 호출
        val resultActions = loginUserRequest(username, password)

        // THEN: BadRequest 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.message").value("password : 비밀번호는 필수 입력값입니다."))
    }

    @Test
    @DisplayName("로그인 - 실패 - 요청 body 누락")
    @Throws(Exception::class)
    fun loginUser6() {
        // WHEN: 요청 body 없이 로그인 API 호출
        val resultActions = mvc.perform(post("/api/users/login")).andDo(print())

        // THEN: BadRequest 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.message").value("값을 입력해주세요."))
    }

    @Test
    @DisplayName("로그아웃 - 성공 - 헤더 인증")
    @Throws(Exception::class)
    fun logoutUser1() {
        // WHEN: 헤더에 토큰을 포함하여 로그아웃 API 호출
        val resultActions = mvc.perform(
            post("/api/users/logout")
                .header("Authorization", "Bearer $validToken")
        )

        // THEN: 로그아웃 성공 응답 및 쿠키 만료 확인
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))

        resultActions.andExpect { mvcResult: MvcResult ->
            val refreshToken = mvcResult.response.getCookie("refreshToken")
            assertThat(refreshToken).isNotNull
            assertThat(refreshToken.maxAge).isLessThanOrEqualTo(0)

            val accessToken = mvcResult.response.getCookie("accessToken")
            assertThat(accessToken).isNotNull
            assertThat(accessToken.maxAge).isLessThanOrEqualTo(0)
        }
    }

    @Test
    @DisplayName("로그아웃 - 성공 - 쿠키 인증")
    @Throws(Exception::class)
    fun logoutUser2() {
        // WHEN: 쿠키에 토큰을 포함하여 로그아웃 API 호출
        val resultActions = mvc.perform(
            post("/api/users/logout")
                .cookie(Cookie("accessToken", validAccessToken), Cookie("refreshToken", validRefreshToken))
        )

        // THEN: 로그아웃 성공 응답 및 쿠키 만료 확인
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))

        resultActions.andExpect { mvcResult: MvcResult ->
            val refreshToken = mvcResult.response.getCookie("refreshToken")
            assertThat(refreshToken).isNotNull
            assertThat(refreshToken.maxAge).isLessThanOrEqualTo(0)

            val accessToken = mvcResult.response.getCookie("accessToken")
            assertThat(accessToken).isNotNull
            assertThat(accessToken.maxAge).isLessThanOrEqualTo(0)
        }
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 - 쿠키 인증 - 유효한 AccessToken + 유효한 RefreshToken")
    @Throws(Exception::class)
    fun me4() {
        // WHEN: 쿠키에 유효한 토큰들을 포함하여 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .cookie(Cookie("accessToken", validAccessToken), Cookie("refreshToken", validRefreshToken))
        ).andDo(print())

        // THEN: 내 정보 조회 성공 응답 및 유저 정보 검증
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."))
        checkUser(resultActions, loginedUser)
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 - 쿠키 인증 - 만료된 AccessToken + 유효한 RefreshToken")
    @Throws(Exception::class)
    fun me5() {
        // WHEN: 만료된 AccessToken과 유효한 RefreshToken으로 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .cookie(Cookie("accessToken", expiredAccessToken), Cookie("refreshToken", validRefreshToken))
        ).andDo(print())

        // THEN: refreshToken으로 AccessToken 재발급 후 내 정보 조회 성공
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."))
        checkUser(resultActions, loginedUser)
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 - 쿠키 인증 - 유효한 AccessToken + 만료된 RefreshToken")
    @Throws(Exception::class)
    fun me6() {
        // WHEN: 유효한 AccessToken과 만료된 RefreshToken으로 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .cookie(Cookie("accessToken", validAccessToken), Cookie("refreshToken", invalidRefreshToken))
        ).andDo(print())

        // THEN: RefreshToken이 만료되었어도 AccessToken으로 정상 조회됨
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."))
        checkUser(resultActions, loginedUser)
    }

    @Test
    @DisplayName("내 정보 조회 - 실패 - 쿠키 인증 - 만료된 AccessToken + 잘못된 RefreshToken")
    @Throws(Exception::class)
    fun me7() {
        // WHEN: 만료된 AccessToken과 잘못된 RefreshToken으로 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .cookie(Cookie("accessToken", expiredAccessToken), Cookie("refreshToken", invalidRefreshToken))
        ).andDo(print())

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("내 정보 조회 - 실패 - 쿠키 인증 - 잘못된 형식 (토큰 하나만 존재)")
    @Throws(Exception::class)
    fun me8() {
        // WHEN: refreshToken만 포함한 상태로 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .cookie(Cookie("refreshToken", validRefreshToken))
        ).andDo(print())

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 - 헤더 인증 - 유효한 RefreshToken + 유효한 AccessToken")
    @Throws(Exception::class)
    fun me9() {
        // WHEN: Authorization 헤더에 유효한 토큰들을 포함하여 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $validToken")
        ).andDo(print())

        // THEN: 내 정보 조회 성공 및 유저 정보 검증
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."))
        checkUser(resultActions, loginedUser)
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 - 헤더 인증 - 만료된 AccessToken + 유효한 RefreshToken")
    @Throws(Exception::class)
    fun me10() {
        // WHEN: 헤더에 만료된 AccessToken과 유효한 RefreshToken을 포함하여 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $validRefreshToken $expiredAccessToken")
        ).andDo(print())

        // THEN: refreshToken을 통해 AccessToken 재발급 후 내 정보 조회 성공
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."))
        checkUser(resultActions, loginedUser)
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 - 헤더 인증 - 유효한 AccessToken + 만료된 RefreshToken")
    @Throws(Exception::class)
    fun me11() {
        // WHEN: 헤더에 유효한 AccessToken과 만료된 RefreshToken을 포함하여 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $invalidRefreshToken $validAccessToken")
        ).andDo(print())

        // THEN: 내 정보 조회 성공
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."))
        checkUser(resultActions, loginedUser)
    }

    @Test
    @DisplayName("내 정보 조회 - 실패 - 헤더 인증 - 만료된 AccessToken + 만료된 RefreshToken")
    @Throws(Exception::class)
    fun me12() {
        // WHEN: 헤더에 만료된 AccessToken과 만료된 RefreshToken을 포함하여 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $invalidRefreshToken $expiredAccessToken")
        ).andDo(print())

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("내 정보 조회 - 실패 - 헤더 인증 - 토큰 하나만 존재")
    @Throws(Exception::class)
    fun me13() {
        // WHEN: Authorization 헤더에 refreshToken만 포함하여 내 정보 조회 API 호출
        val resultActions = mvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $validRefreshToken")
        ).andDo(print())

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }

    // Helper: 토큰 재발급 요청 (GIVEN)
    @Throws(Exception::class)
    private fun refreshAccessTokenRequest(refreshToken: String?): ResultActions {
        return mvc.perform(
            post("/api/users/refresh")
                .content(
                    """
                    {
                      "refreshToken": "$refreshToken"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    @Throws(Exception::class)
    fun refreshAccessToken1() {
        // WHEN: 유효한 refreshToken으로 토큰 재발급 API 호출
        val resultActions = refreshAccessTokenRequest(validRefreshToken)

        // THEN: AccessToken 재발급 성공 응답 및 쿠키 검증
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("AccessToken이 재발급되었습니다."))
            .andExpect(jsonPath("$.data").exists())

        resultActions.andExpect { mvcResult: MvcResult ->
            val accessToken = mvcResult.response.getCookie("accessToken")
            assertThat(accessToken).isNotNull
            assertThat(accessToken.name).isEqualTo("accessToken")
            assertThat(accessToken.value).isNotBlank
            assertThat(accessToken.path).isEqualTo("/")
            assertThat(accessToken.isHttpOnly).isTrue
            assertThat(accessToken.secure).isTrue
        }
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 - 요청 body 누락")
    @Throws(Exception::class)
    fun refreshAccessToken2() {
        // WHEN: 요청 body 없이 토큰 재발급 API 호출
        val resultActions = mvc.perform(
            post("/api/users/refresh")
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        // THEN: BadRequest 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.message").value("값을 입력해주세요."))
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 - refreshToken이 빈 문자열")
    @Throws(Exception::class)
    fun refreshAccessToken3() {
        // GIVEN: 빈 refreshToken 제공
        val token = " "
        // WHEN: 토큰 재발급 API 호출
        val resultActions = refreshAccessTokenRequest(token)

        // THEN: BadRequest 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(jsonPath("$.message").value("refreshToken : refreshToken을 입력해주세요."))
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 - 존재하지 않는 refreshToken")
    @Throws(Exception::class)
    fun refreshAccessToken4() {
        // GIVEN: 존재하지 않는 refreshToken 제공
        val fakeRefreshToken = "invalid_refresh_token"
        // WHEN: 토큰 재발급 API 호출
        val resultActions = refreshAccessTokenRequest(fakeRefreshToken)

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("유효하지 않은 RefreshToken입니다."))
    }

    @Test
    @DisplayName("내 정보 수정 - 성공")
    @Throws(Exception::class)
    fun updateProfile1() {
        // GIVEN: 새로운 프로필 정보 준비 (이메일 인증 완료 가정)
        val newNickname = "새로운닉네임"
        val newAddress = "서울시 서초구"
        val newEmail = "newemail@example.com"
        emailService.saveVerificationCode(newEmail, "verified")

        // WHEN: 내 정보 수정 API 호출
        val resultActions = mvc.perform(
            put("/api/users/me")
                .header("Authorization", "Bearer $validToken")
                .content(
                    """
                    {
                      "nickname": "$newNickname",
                      "address": "$newAddress",
                      "email": "$newEmail"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN: 내 정보 수정 성공 응답 및 DB 변경 검증
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("사용자 정보가 성공적으로 수정되었습니다."))

        val updatedUser = userService.getUserByUsername(loginedUser.username).get()
        assertThat(updatedUser.nickname).isEqualTo(newNickname)
        assertThat(updatedUser.address).isEqualTo(newAddress)
        assertThat(updatedUser.email).isEqualTo(newEmail)
    }

    @Test
    @DisplayName("내 정보 수정 - 실패 - 이메일 중복")
    @Throws(Exception::class)
    fun updateProfile2() {
        // GIVEN: 이미 존재하는 이메일("user2@gmail.com") 사용
        val duplicateEmail = "user2@gmail.com"

        // WHEN: 내 정보 수정 API 호출
        val resultActions = mvc.perform(
            put("/api/users/me")
                .header("Authorization", "Bearer $validToken")
                .content(
                    """
                    {
                      "email": "$duplicateEmail"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN: Conflict 응답 확인 (이메일 중복)
        resultActions
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("409-2"))
            .andExpect(jsonPath("$.message").value("이미 사용중인 이메일입니다."))
    }

    @Test
    @DisplayName("내 정보 수정 - 실패 - 이메일 인증 안함")
    @Throws(Exception::class)
    fun updateProfile3() {
        // GIVEN: 인증되지 않은 이메일 사용
        val duplicateEmail = "invaild@gmail.com"

        // WHEN: 내 정보 수정 API 호출
        val resultActions = mvc.perform(
            put("/api/users/me")
                .header("Authorization", "Bearer $validToken")
                .content(
                    """
                    {
                      "email": "$duplicateEmail"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN: Conflict 응답 확인 (이메일 미인증)
        resultActions
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("409"))
            .andExpect(jsonPath("$.message").value("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요."))
    }

    @Test
    @DisplayName("내 정보 수정 - 실패 - 닉네임 중복")
    @Throws(Exception::class)
    fun updateProfile4() {
        // GIVEN: 이미 사용 중인 닉네임("user3") 사용
        val duplicateNickname = "user3"

        // WHEN: 내 정보 수정 API 호출
        val resultActions = mvc.perform(
            put("/api/users/me")
                .header("Authorization", "Bearer $validToken")
                .content(
                    """
                    {
                      "nickname": "$duplicateNickname"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN: BadRequest 응답 확인 (닉네임 중복)
        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-NICKNAME-ALREADY-EXISTS"))
            .andExpect(jsonPath("$.message").value("이미 사용중인 닉네임입니다."))
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    @Throws(Exception::class)
    fun deleteProfile1() {
        // WHEN: 회원 탈퇴 API 호출
        val resultActions = mvc.perform(
            delete("/api/users/me")
                .header("Authorization", "Bearer $validToken")
        )

        // THEN: 회원 탈퇴 성공 응답 및 DB 삭제 여부 검증
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("회원 탈퇴 성공"))

        val isDeleted = isUserDeleted(loginedUser.username)
        assertThat(isDeleted).isTrue()
    }

    // Helper: 유저 삭제 여부 확인 (테스트 환경에서 DB 조회)
    private fun isUserDeleted(username: String): Boolean {
        return userService.getUserByUsername(username).isEmpty
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (토큰 없음)")
    @Throws(Exception::class)
    fun deleteProfile2() {
        // WHEN: 토큰 없이 회원 탈퇴 API 호출
        val resultActions = mvc.perform(delete("/api/users/me")).andDo(print())

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (잘못된 토큰)")
    @Throws(Exception::class)
    fun deleteProfile3() {
        // WHEN: 잘못된 토큰을 사용하여 회원 탈퇴 API 호출
        val resultActions = mvc.perform(
            delete("/api/users/me")
                .header("Authorization", "Bearer wrong-token")
        )

        // THEN: Unauthorized 응답 및 에러 메시지 검증
        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("401-1"))
            .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
    }
}
