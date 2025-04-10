package com.NBE_4_5_2.Team5.domain.post.comment.controller

import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment
import com.NBE_4_5_2.Team5.domain.post.comment.repository.CommentRepository
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import util.Util

@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@Order(102)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostCommentControllerTest {

    @Autowired
    lateinit var util: Util

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var productPostRepository: ProductPostRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        // 이메일 인증이 통과된 것으로 가정
        emailService.saveAuthenticationCode("emasdfasdail", "verified")
    }

    @Test
    fun writeComment() {
        // given
        val author: User = userService.createUser(
            "useasdfasde",
            "password",
            "emasdfasdail",
            "nicsdfdakname",
            "address",
            "url"
        )

        val productPost: ProductPost = productPostRepository.save(
            ProductPost(author, "name", 5000, "title", "content", "url", 50F, 50F)
        )

        val cookieMap: Map<String, Cookie> = login(author.username, "password")

        // when
        val action: ResultActions = mockMvc.perform(
            post("/api/posts/${productPost.id}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
                .content(
                    """
                    {
                        "content": "wow"
                    }
                    """.trimIndent()
                )
        )

        // then
        action.andExpect(status().isOk)
            .andExpect(handler().handlerType(PostCommentController::class.java))
            .andExpect(handler().methodName("writeComment"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.message").value("댓글 작성 성공."))
            .andExpect(jsonPath("$.data.content").value("wow"))
            .andExpect(jsonPath("$.data.author.id").value(author.id))
    }

    @Test
    fun updateTest() {
        // given
        val author: User = userService.createUser(
            "userasdasname",
            "password",
            "emasdfasdail",
            "nicknfasdfasdaame",
            "address",
            "url"
        )

        val productPost: ProductPost = productPostRepository.save(
            ProductPost(author, "name", 5000, "title", "content", "url", 50F, 50F)
        )

        val cookieMap: Map<String, Cookie> = login(author.username, "password")

        val contentResult: String = mockMvc.perform(
            post("/api/posts/${productPost.id}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
                .content(
                    """
                    {
                        "content": "before"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").exists())
            .andReturn().response.contentAsString

        val commentId: String = objectMapper.readTree(contentResult).get("data").get("id").asText()

        // when
        val action: ResultActions = mockMvc.perform(
            put("/api/posts/${productPost.id}/comments/$commentId")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
                .content(
                    """
                    {
                        "content": "changedContent"
                    }
                    """.trimIndent()
                )
        )

        // then
        action.andExpect(status().isOk)
            .andExpect(handler().handlerType(PostCommentController::class.java))
            .andExpect(handler().methodName("updateComment"))
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.data.content").value("changedContent"))
            .andExpect(jsonPath("$.message").value("comment 수정 성공."))
    }

    @Test
    fun deleteTest() {
        // given
        val author: User = userService.createUser(
            "userdasfasdname",
            "password",
            "emasdfasdail",
            "nicknawqeqwdcame",
            "address",
            "url"
        )

        val productPost: ProductPost = productPostRepository.save(
            ProductPost(author, "name", 5000, "title", "content", "url", 50F, 50F)
        )

        val cookieMap: Map<String, Cookie> = login(author.username, "password")

        val result: String = mockMvc.perform(
            post("/api/posts/${productPost.id}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
                .content(
                    """
                    {
                        "content": "wow"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val commentId: String = objectMapper.readTree(result).get("data").get("id").asText()

        // when
        val action: ResultActions = mockMvc.perform(
            delete("/api/posts/${productPost.id}/comments/$commentId")
                .cookie(cookieMap["accessToken"], cookieMap["refreshToken"])
        )

        // then
        action.andExpect(status().isNoContent)
            .andExpect(handler().handlerType(PostCommentController::class.java))
            .andExpect(handler().methodName("deleteComment"))
            .andExpect(jsonPath("$.code").value("204-1"))

        val byId: java.util.Optional<Comment> = commentRepository.findById(commentId)
        Assertions.assertThat(byId).isEmpty
    }

    private fun login(username: String, password: String): Map<String, Cookie> {
        val response: MockHttpServletResponse = mockMvc.perform(
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
            .andExpect(status().isOk)
            .andReturn().response

        return mapOf(
            "accessToken" to requireNotNull(response.getCookie("accessToken")),
            "refreshToken" to requireNotNull(response.getCookie("refreshToken"))
        )
    }
}
