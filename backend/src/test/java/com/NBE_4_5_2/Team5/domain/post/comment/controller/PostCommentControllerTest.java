package com.NBE_4_5_2.Team5.domain.post.comment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.TestConfig;
import com.NBE_4_5_2.Team5.Util;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class PostCommentControllerTest {

	@Autowired
	private Util util;
	@Autowired
	private ProductPostService productPostService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ProductPostRepository productPostRepository;
	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		util.truncateAllTables();
	}

	@Test
	void writeComment() throws Exception {
		// given
		User author = userService.signup("username", "password", "email", "nickname", "address", "url");

		ProductPost productPost = productPostRepository.save(
			ProductPost.create(author, "name", 5000, "title", "content", "url", 50F, 50F)
		);

		MockHttpServletResponse response = mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"username": "username",
						"password": "password"
					}""")).andExpect(status().isOk())
			.andReturn().getResponse();

		Map<String, Cookie> cookieMap = Map.of("accessToken", Objects.requireNonNull(response.getCookie("accessToken")),
			"refreshToken", Objects.requireNonNull(response.getCookie("refreshToken")));

		// when
		ResultActions action = mockMvc.perform(post("/api/posts/%s/comments".formatted(productPost.getId()))
			.contentType(MediaType.APPLICATION_JSON)
			.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken"))
			.content("""
				{
					"content": "wow"
				}"""));

		action
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(PostCommentController.class))
			.andExpect(handler().methodName("writeComment"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("댓글 작성 성공."))
			.andExpect(jsonPath("$.data.content").value("wow"))
			.andExpect(jsonPath("$.data.author.id").value(author.getId()));
	}
}