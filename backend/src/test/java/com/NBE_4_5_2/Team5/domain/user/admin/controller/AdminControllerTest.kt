package com.NBE_4_5_2.Team5.domain.user.admin.controller

import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.domain.user.admin.repository.BanListRepository
import com.NBE_4_5_2.Team5.domain.user.admin.service.AdminService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.NBE_4_5_2.Team5.global.exception.post.product.ProductPostNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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
            MockMvcRequestBuilders.post("/api/admin/notices")
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
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(AdminController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("writeNotice"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("공지사항 등록 성공."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(notice.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(notice.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(notice.content))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.admin.id").value(admin.id))
    }

    @Throws(Exception::class)
    private fun login(username: String, password: String): Map<String, Cookie> {
        val cookies = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users/login")
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
            .andExpect(MockMvcResultMatchers.status().isOk())
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
            MockMvcRequestBuilders.post("/api/admin/users/${user.id}/ban")
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
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(AdminController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("banUser"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저 정지 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.banListId").value(banList.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.reason").value(banList.reason))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(banList.bannedUser.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.banCount").value(foundedUser.get().blockedCount))
        Assertions.assertThat(foundedUser.get().blocked).isTrue()
        Assertions.assertThat(foundedUser.get().blockedCount).isEqualTo(1)
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
            MockMvcRequestBuilders.delete("/api/admin/posts/$id")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then

        // 삭제된 product post 조회 시 오류 발생해야 한다.
        Assertions.assertThatThrownBy { productPostService.getPost(id) }
            .isInstanceOf(ProductPostNotFoundException::class.java)

        result
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("204-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 삭제 성공."))
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
            MockMvcRequestBuilders.post("/api/admin/notices")
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
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("공지사항 등록 성공."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(notice.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(notice.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(notice.content))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.admin.id").value(admin.id))
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
            MockMvcRequestBuilders.post("/api/admin/users/${user.id}/ban")
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
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(AdminController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("banUser"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저 정지 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.banListId").value(banList.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.reason").value(banList.reason))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(banList.bannedUser.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.banCount").value(foundedUser.get().blockedCount))
        Assertions.assertThat(foundedUser.get().blocked).isTrue()
        Assertions.assertThat(foundedUser.get().blockedCount).isEqualTo(1)
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
            MockMvcRequestBuilders.delete("/api/admin/posts/$id")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then

        // 삭제된 product post 조회 시 오류 발생해야 한다.
        Assertions.assertThatThrownBy { productPostService.getPost(id) }
            .isInstanceOf(ProductPostNotFoundException::class.java)

        result
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("204-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 삭제 성공."))
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
            MockMvcRequestBuilders.post("/api/admin/signup")
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
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("관리자 회원가입 성공."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("admin3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value("관리자3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value("admin3@gmail.com"))
    }

    @Test
    @Throws(Exception::class)
    fun signUpAdminWithoutEmailAuthentication() {
        // given

        val cookieMap = login("admin1", "password1") // 메인 관리자로 로그인
        // 이메일 인증을 하지 않은 상태

        // when: 인증이 완료되지 않은 이메일로 관리자 회원가입 시도
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/admin/signup")
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
        result.andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("409"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요."))
    }

    @Test
    @Throws(Exception::class)
    fun signUpAdminWithInvalidInput() {
        // given
        // 메인 관리자로 로그인 (SUPER_ADMIN 권한)
        val cookieMap = login("admin1", "password1")
        val username: String = "admin!@#" // 특수문자 포함되어 유효성 검증에 실패
        val password: String = "admin3@"
        val nickname: String = "1" // 닉네임이 2글자 미만
        val email: String = "invalid-email" // 올바른 이메일 형식 아님

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
            MockMvcRequestBuilders.post("/api/admin/signup")
                .content(invalidContent)
                .contentType("application/json")
                .characterEncoding("utf-8")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then: 유효성 검증 실패로 400 Bad Request 발생
        result
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.message").value(
                    """
                        email : 올바른 이메일 형식이 아닙니다.
                        nickname : 닉네임은 2~20자 사이여야 합니다.
                        username : 아이디는 영문과 숫자만 사용할 수 있습니다.
                    """.trimIndent().trimEnd()
                )
            )
    }


}