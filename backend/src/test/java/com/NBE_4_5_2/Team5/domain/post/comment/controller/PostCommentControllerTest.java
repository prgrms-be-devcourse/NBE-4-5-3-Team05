package com.NBE_4_5_2.Team5.domain.post.comment.controller;

import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment;
import com.NBE_4_5_2.Team5.domain.post.comment.repository.CommentRepository;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.NBE_4_5_2.Team5.global.config.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@Order(102)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostCommentControllerTest  {

	@Autowired
	private Util util;
	@Autowired
	private UserService userService;
	@Autowired
	private ProductPostRepository productPostRepository;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	EmailService emailService;

	@BeforeEach
	void setUp() {
		emailService.saveVerificationCode("emasdfasdail", "verified"); // 이메일 인증이 통과되었다고 가정
	}

	@Test
	void writeComment() throws Exception {
		// given
		User author = userService.createUser("useasdfasde", "password", "emasdfasdail", "nicsdfdakname", "address",
				"url");

		ProductPost productPost = productPostRepository.save(
				ProductPost.create(author, "name", 5000, "title", "content", "url", 50F, 50F)
		);

		Map<String, Cookie> cookieMap = login(author.getUsername(), "password");

		// when
		ResultActions action = mockMvc.perform(post("/api/posts/%s/comments".formatted(productPost.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken"))
				.content("""
				{
					"content": "wow"
				}"""));

		//then
		action
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(PostCommentController.class))
				.andExpect(handler().methodName("writeComment"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.message").value("댓글 작성 성공."))
				.andExpect(jsonPath("$.data.content").value("wow"))
				.andExpect(jsonPath("$.data.author.id").value(author.getId()));
	}

	@Test
	void updateTest() throws Exception {
		//given
		User author = userService.createUser("userasdasname", "password", "emasdfasdail", "nicknfasdfasdaame",
				"address",
				"url");

		ProductPost productPost = productPostRepository.save(
				ProductPost.create(author, "name", 5000, "title", "content", "url", 50F, 50F)
		);

		Map<String, Cookie> cookieMap = login(author.getUsername(), "password");

		String content = mockMvc.perform(post("/api/posts/%s/comments".formatted(productPost.getId()))
						.contentType(MediaType.APPLICATION_JSON)
						.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken"))
						.content("""
					{
						"content": "before"
					}
					"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").exists())
				.andReturn().getResponse().getContentAsString();

		String commentId = objectMapper.readTree(content).get("data").get("id").asText();

		//when

		ResultActions action = mockMvc.perform(
				put("/api/posts/%s/comments/%s".formatted(productPost.getId(), commentId))
						.contentType(MediaType.APPLICATION_JSON)
						.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken"))
						.content("""
					{
						"content": "changedContent"
					}"""));
		//then

		action
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(PostCommentController.class))
				.andExpect(handler().methodName("updateComment"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.data.content").value("changedContent"))
				.andExpect(jsonPath("$.message").value("comment 수정 성공."));

	}

	private Map<String, Cookie> login(String username, String password) throws Exception {
		MockHttpServletResponse response = mockMvc.perform(post("/api/users/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
					{
						"username": "%s",
						"password": "%s"
					}""".formatted(username, password))).andExpect(status().isOk())
				.andReturn().getResponse();

		return Map.of("accessToken", Objects.requireNonNull(response.getCookie("accessToken")),
				"refreshToken", Objects.requireNonNull(response.getCookie("refreshToken")));
	}

	@Test
	void deleteTest() throws Exception {
		//given
		User author = userService.createUser("userdasfasdname", "password", "emasdfasdail", "nicknawqeqwdcame",
				"address", "url");

		ProductPost productPost = productPostRepository.save(
				ProductPost.create(author, "name", 5000, "title", "content", "url", 50F, 50F)
		);

		Map<String, Cookie> cookieMap = login(author.getUsername(), "password");

		String result = mockMvc.perform(post("/api/posts/%s/comments".formatted(productPost.getId()))
						.contentType(MediaType.APPLICATION_JSON)
						.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken"))
						.content("""
					{
						"content": "wow"
					}"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		String commentId = objectMapper.readTree(result).get("data").get("id").asText();

		//when
		// 삭제
		ResultActions action = mockMvc.perform(
				delete("/api/posts/%s/comments/%s".formatted(productPost.getId(), commentId))
						.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken")));

		//then
		// 204 return

		action
				.andExpect(status().isNoContent())
				.andExpect(handler().handlerType(PostCommentController.class))
				.andExpect(handler().methodName("deleteComment"))
				.andExpect(jsonPath("$.code").value("204-1"));

		Optional<Comment> byId = commentRepository.findById(commentId);
		Assertions.assertThat(byId).isEmpty();
	}
}