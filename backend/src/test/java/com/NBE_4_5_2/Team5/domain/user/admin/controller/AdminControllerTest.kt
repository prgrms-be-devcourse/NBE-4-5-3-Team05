package com.NBE_4_5_2.Team5.domain.user.admin.controller

import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.domain.user.admin.repository.BanListRepository
import com.NBE_4_5_2.Team5.domain.user.admin.service.AdminService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.NBE_4_5_2.Team5.global.exception.post.product.ProductPostNotFoundException
import com.NBE_4_5_2.Team5.global.exception.user.AdminNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.nio.charset.StandardCharsets
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AdminControllerTest(
    @Autowired
    private val mockMvc: MockMvc,

    @Autowired
    private val banListRepository: BanListRepository,

    @Autowired
    private val objectMapper: ObjectMapper,

    @Autowired
    private val userService: UserService,

    @Autowired
    private val productPostService: ProductPostService,

    @Autowired
    private val adminService: AdminService,
) {

    @Test
    @Throws(Exception::class)
    fun writeNotice() {
        //given
        // 관리자로 로그인
        val cookieMap = login("user4", "user41234@")

        // when
        // API 호출
        val perform = mockMvc.perform(
            post("/api/admin/notices")
                .content(
                    """
				{
					"title": "공지 제목",
					"content": "공지 내용"
				}
				""".trimIndent()
                )
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        val id = objectMapper.readTree(perform.andReturn().response.contentAsString)["data"]["id"].asText()

        val notice = adminService.getNotice(id)

        val admin = userService.getUserByUsername("user4")
            .orElseThrow { RuntimeException() }

        // then
        perform
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(AdminController::class.java))
            .andExpect(handler().methodName("writeNotice"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("공지사항 등록 성공."))
            .andExpect(jsonPath("$.data.id").value(notice.id))
            .andExpect(jsonPath("$.data.title").value(notice.title))
            .andExpect(jsonPath("$.data.content").value(notice.content))
            .andExpect(jsonPath("$.data.admin.id").value(admin.id))
    }

    @Throws(Exception::class)
    private fun login(username: String, password: String): Map<String, Cookie> {
        val cookies = mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
					{
						"username": "$username",
						"password": "$password"
					}
					""".trimIndent()
                )
        )
            .andExpect(status().isOk())
            .andReturn().response.cookies

        val cookieMap: MutableMap<String, Cookie> = HashMap()

        Arrays.stream(cookies)
            .filter { cookie: Cookie -> cookie.name == "accessToken" || cookie.name == "refreshToken" }
            .forEach { cookie: Cookie -> cookieMap[cookie.name] = cookie }
        return cookieMap
    }

    @Test
    @Throws(Exception::class)
    fun banUser() {
        //given
        // 관리자 로그인
        val cookieMap = login("user4", "user41234@")

        // when
        // user1을 밴함
        val user = userService.getUserByUsername("user1")
            .orElseThrow { RuntimeException() }

        val perform = mockMvc.perform(
            post("/api/admin/users/${user.id}/ban")
                .content(
                    """
				{
					"reason": "기분이 나빠서"
				}
				""".trimIndent()
                )
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        val id = objectMapper.readTree(perform.andReturn().response.contentAsString)["data"]["banListId"].asText()

        val banList = banListRepository.findById(id).get()

        // then
        val foundedUser = userService.getUserByUsername("user1")

        perform
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(AdminController::class.java))
            .andExpect(handler().methodName("banUser"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("유저 정지 성공"))
            .andExpect(jsonPath("$.data.banListId").value(banList.id))
            .andExpect(jsonPath("$.data.reason").value(banList.reason))
            .andExpect(jsonPath("$.data.userId").value(banList.bannedUser.id))
            .andExpect(jsonPath("$.data.banCount").value(foundedUser.get().blockedCount))
        assertThat(foundedUser.get().blocked).isTrue()
        assertThat(foundedUser.get().blockedCount).isEqualTo(1)
    }

    @Test
    @Throws(Exception::class)
    fun deletePost() {
        //given
        // 로그인
        val cookieMap = login("user4", "user41234@")

        //when
        // id를 가진 post를 삭제
        val id = productPostService.getPosts(1, 1, "", "asc", 0, 1000000, emptyList())
            .items[0].id

        val result = mockMvc.perform(
            delete("/api/admin/posts/$id")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then

        // 삭제된 product post 조회 시 오류 발생해야 한다.
        assertThatThrownBy { productPostService.getPost(id) }
            .isInstanceOf(ProductPostNotFoundException::class.java)

        result
            .andExpect(status().isNoContent)
            .andExpect(jsonPath("$.code").value("204-1"))
            .andExpect(jsonPath("$.message").value("게시글 삭제 성공."))
    }

    @Test
    @Throws(Exception::class)
    fun writeNoticeBySuperAdmin() {
        //given
        // 메인 관리자로 로그인
        val cookieMap = login("admin1", "password1")

        // when
        // API 호출
        val perform = mockMvc.perform(
            post("/api/admin/notices")
                .content(
                    """
				{
					"title": "공지 제목",
					"content": "공지 내용"
				}
				""".trimIndent()
                )
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        val id = objectMapper.readTree(perform.andReturn().response.contentAsString)["data"]["id"].asText()

        val notice = adminService.getNotice(id)

        val admin = userService.getUserByUsername("admin1")
            .orElseThrow { RuntimeException() }

        // then
        perform
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("공지사항 등록 성공."))
            .andExpect(jsonPath("$.data.id").value(notice.id))
            .andExpect(jsonPath("$.data.title").value(notice.title))
            .andExpect(jsonPath("$.data.content").value(notice.content))
            .andExpect(jsonPath("$.data.admin.id").value(admin.id))
    }

    @Test
    @Throws(Exception::class)
    fun banUserBySuperAdmin() {
        //given
        // 메인 관리자로 로그인
        val cookieMap = login("admin1", "password1")

        // when
        // user1을 밴함
        val user = userService.getUserByUsername("user1")
            .orElseThrow { RuntimeException() }

        val perform = mockMvc.perform(
            post("/api/admin/users/${user.id}/ban")
                .content(
                    """
				{
					"reason": "기분이 나빠서"
				}
				""".trimIndent()
                )
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        val id = objectMapper.readTree(perform.andReturn().response.contentAsString)["data"]["banListId"].asText()

        val banList = banListRepository.findById(id).get()

        // then
        val foundedUser = userService.getUserByUsername("user1")

        perform
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(AdminController::class.java))
            .andExpect(handler().methodName("banUser"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("유저 정지 성공"))
            .andExpect(jsonPath("$.data.banListId").value(banList.id))
            .andExpect(jsonPath("$.data.reason").value(banList.reason))
            .andExpect(jsonPath("$.data.userId").value(banList.bannedUser.id))
            .andExpect(jsonPath("$.data.banCount").value(foundedUser.get().blockedCount))
        assertThat(foundedUser.get().blocked).isTrue()
        assertThat(foundedUser.get().blockedCount).isEqualTo(1)
    }

    @Test
    @Throws(Exception::class)
    fun deletePostBySuperAdmin() {
        //given
        // 메인 관리자로 로그인
        val cookieMap = login("admin1", "password1")

        //when
        // id를 가진 post를 삭제
        val id = productPostService.getPosts(1, 1, "", "asc", 0, 1000000, emptyList())
            .items[0].id

        val result = mockMvc.perform(
            delete("/api/admin/posts/$id")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then

        // 삭제된 product post 조회 시 오류 발생해야 한다.
        assertThatThrownBy { productPostService.getPost(id) }
            .isInstanceOf(ProductPostNotFoundException::class.java)

        result
            .andExpect(status().isNoContent)
            .andExpect(jsonPath("$.code").value("204-1"))
            .andExpect(jsonPath("$.message").value("게시글 삭제 성공."))
    }

    @Test
    @Throws(Exception::class)
    fun signUpAdminBySuperAdmin() {
        //given
        // 메인 관리자로 로그인
        val cookieMap = login("admin1", "password1")
        userService.saveAuthenticationCode("admin3@gmail.com", "verified") // 이메일 인증이 완료되었다 가정

        //when
        val result = mockMvc.perform(
            post("/api/admin/signup")
                .content(
                    """
				{
					"username": "admin3",
                    "password": "admin3@",
                    "nickname": "관리자3",
                    "email": "admin3@gmail.com"
				}
				""".trimIndent()
                )
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("관리자 회원가입 성공."))
            .andExpect(jsonPath("$.data.username").value("admin3"))
            .andExpect(jsonPath("$.data.nickname").value("관리자3"))
            .andExpect(jsonPath("$.data.email").value("admin3@gmail.com"))
    }

    @Test
    @Throws(Exception::class)
    fun signUpAdminWithoutEmailAuthentication() {
        // given

        val cookieMap = login("admin1", "password1") // 메인 관리자로 로그인
        // 이메일 인증을 하지 않은 상태

        // when: 인증이 완료되지 않은 이메일로 관리자 회원가입 시도
        val result = mockMvc.perform(
            post("/api/admin/signup")
                .content(
                    """
                {
                    "username": "admin3",
                    "password": "admin3@",
                    "nickname": "관리자3",
                    "email": "admin3@gmail.com"
                }
                """.trimIndent()
                )
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then: 이메일 인증 미완료 오류
        result.andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("409"))
            .andExpect(jsonPath("$.message").value("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요."))
    }

    @Test
    @Throws(Exception::class)
    fun signUpAdminWithInvalidInput() {
        // given
        // 메인 관리자로 로그인 (SUPER_ADMIN 권한)
        val cookieMap = login("admin1", "password1")
        val username = "admin!@#" // 특수문자 포함되어 유효성 검증에 실패
        val password = "admin3@"
        val nickname = "1" // 닉네임이 2글자 미만
        val email = "invalid-email" // 올바른 이메일 형식 아님

        val invalidContent = """
        {
            "username": "${username}",  
            "password": "${password}",
            "nickname": "${nickname}",         
            "email": "${email}" 
        }
    """.trimIndent()

        // when: 잘못된 입력값으로 관리자 회원가입 시도
        val result = mockMvc.perform(
            post("/api/admin/signup")
                .content(invalidContent)
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then: 유효성 검증 실패로 400 Bad Request 발생
        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400-1"))
            .andExpect(
                jsonPath("$.message").value(
                    """
                        email : 올바른 이메일 형식이 아닙니다.
                        nickname : 닉네임은 2~20자 사이여야 합니다.
                        username : 아이디는 영문과 숫자만 사용할 수 있습니다.
                    """.trimIndent().trimEnd()
                )
            )
    }

    @Test
    @Throws(Exception::class)
    fun deleteAdminBySuperAdmin() {
        // given
        // SUPER_ADMIN 로그인
        val cookieMap = login("admin1", "password1")

        userService.saveAuthenticationCode("adminToDelete@gmail.com", "verified")

        // 삭제 대상 관리자 신규 생성
        val signupResult = mockMvc.perform(
            post("/api/admin/signup")
                .content(
                    """
                    {
                        "username": "adminToDelete",
                        "password": "adminToDelete@",
                        "nickname": "삭제대상관리자",
                        "email": "adminToDelete@gmail.com"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        ).andReturn()

        val newAdminId = objectMapper.readTree(signupResult.response.contentAsString)
            .get("data").get("id").asText()

        // when: 생성된 관리자 계정을 삭제 요청합니다.
        val deleteResult = mockMvc.perform(
            delete("/api/admin/$newAdminId")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        ).andReturn()

        // then: 삭제 후 해당 관리자 계정을 조회하면 AdminNotFoundException 발생
        assertThatThrownBy {
            userService.getUserByUsername("adminToDelete")
                .orElseThrow { AdminNotFoundException("404", "관리자를 찾을 수 없습니다") }
        }.isInstanceOf(AdminNotFoundException::class.java)

        val json = objectMapper.readTree(deleteResult.response.contentAsString)
        assertThat(json.get("code").asText()).isEqualTo("200-1")
        assertThat(json.get("message").asText()).isEqualTo("관리자 삭제 성공.")
        assertThat(deleteResult.response.status).isEqualTo(200)
    }

    @Throws(Exception::class)
    private fun createAdminRequest(
        username: String,
        password: String,
        email: String,
        nickname: String,
        cookieMap: Map<String, Cookie>
    ): ResultActions {
        return mockMvc.perform(
            post("/api/admin/signup")
                .content(
                    """
                {
                  "username": "$username",
                  "password": "$password",
                  "nickname": "$nickname",
                  "email": "$email"
                }
                """.trimIndent()
                )
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        ).andDo(print())
    }


    @Test
    @Throws(Exception::class)
    fun getAdminListBySuperAdmin() {
        // given
        // SUPER_ADMIN 계정으로 로그인 (admin1, password1)
        val cookieMap = login("admin1", "password1")

        // 테스트를 위해 신규 관리자 계정을 생성합니다.
        userService.saveAuthenticationCode("adminA@gmail.com", "verified")
        userService.saveAuthenticationCode("adminB@gmail.com", "verified")
        userService.saveAuthenticationCode("adminC@gmail.com", "verified")

        // 생성할 관리자 3명의 데이터
        val adminData = listOf(
            Triple("adminA", "adminA@", "adminA@gmail.com") to "관리자 A",
            Triple("adminB", "adminB@", "adminB@gmail.com") to "관리자 B",
            Triple("adminC", "adminC@", "adminC@gmail.com") to "관리자 C"
        )

        // 각 관리자 생성 요청 실행 (쿠키 정보를 함께 전달)
        adminData.forEach { (credentials, nickname) ->
            val (username, password, email) = credentials
            createAdminRequest(username, password, email, nickname, cookieMap)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.message").value("관리자 회원가입 성공."))
        }

        // when: 관리자 리스트 조회 API 호출 (페이지 0, 사이즈 10)
        val mvcResult = mockMvc.perform(
            get("/api/admin/admins")
                .param("page", "0")
                .param("size", "10")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        ).andReturn()

        // then: 응답 JSON 검증
        val json = objectMapper.readTree(mvcResult.response.contentAsString)
        assertThat(json.get("code").asText()).isEqualTo("200-1")
        assertThat(json.get("message").asText()).isEqualTo("관리자 리스트 조회 성공.")

        val dataNode = json.get("data")
        assertThat(dataNode.get("content").isArray()).isTrue
        assertThat(dataNode.get("content").size()).isGreaterThanOrEqualTo(1)

        dataNode.get("content").forEach { adminNode ->
            val role = adminNode.get("role").asText()
            assertThat(role.equals("ADMIN", ignoreCase = true) || role.equals("SUPER_ADMIN", ignoreCase = true))
                .withFailMessage("관리자 리스트 항목의 role이 ADMIN 또는 SUPER_ADMIN 이어야 합니다. 현재 role: $role")
                .isTrue
        }
    }

}