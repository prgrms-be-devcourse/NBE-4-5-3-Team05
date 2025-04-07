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
import org.junit.jupiter.api.Order
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
@Order(100)
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
        val id = productPostService.getPosts(1, 1, "", "asc")
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
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("204-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 삭제 성공."))
    }
}