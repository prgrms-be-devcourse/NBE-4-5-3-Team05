package com.NBE_4_5_2.Team5.domain.user.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.admin.dto.NoticeResBody;
import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList;
import com.NBE_4_5_2.Team5.domain.user.admin.repository.BanListRepository;
import com.NBE_4_5_2.Team5.domain.user.admin.service.AdminService;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.NBE_4_5_2.Team5.global.exception.post.product.ProductPostNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@Order(100)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private BanListRepository banListRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserService userService;
	@Autowired
	private ProductPostService productPostService;
	@Autowired
	private AdminService adminService;

	@Test
	void writeNotice() throws Exception {
		//given
		// 관리자로 로그인
		Map<String, Cookie> cookieMap = login("user4", "user41234@");

		// when
		// API 호출
		ResultActions perform = mockMvc.perform(post("/api/admin/notices")
			.content("""
				{
					"title": "공지 제목",
					"content": "공지 내용"
				}""")
			.contentType("application/json")
			.characterEncoding("utf-8")
			.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken")));

		String id = objectMapper.readTree(perform.andReturn().getResponse().getContentAsString())
			.get("data").get("id").asText();

		NoticeResBody notice = adminService.getNotice(id);

		User admin = userService.getUserByUsername("user4")
			.orElseThrow(() -> new RuntimeException());

		// then
		perform
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("writeNotice"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("공지사항 등록 성공."))
			.andExpect(jsonPath("$.data.id").value(notice.id()))
			.andExpect(jsonPath("$.data.title").value(notice.title()))
			.andExpect(jsonPath("$.data.content").value(notice.content()))
			.andExpect(jsonPath("$.data.admin.id").value(admin.getId()));
	}

	private Map<String, Cookie> login(String username, String password) throws Exception {
		Cookie[] cookies = mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"username": "%s",
						"password": "%s"
					}""".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getCookies();

		Map<String, Cookie> cookieMap = new HashMap<>();

		Arrays.stream(cookies)
			.filter(cookie -> cookie.getName().equals("accessToken") || cookie.getName().equals("refreshToken"))
			.forEach(cookie -> cookieMap.put(cookie.getName(), cookie));
		return cookieMap;
	}

	@Test
	void banUser() throws Exception {
		//given
		// 관리자 로그인
		Map<String, Cookie> cookieMap = login("user4", "user41234@");

		// when
		// user1을 밴함

		User user = userService.getUserByUsername("user1")
			.orElseThrow(() -> new RuntimeException());

		ResultActions perform = mockMvc.perform(post("/api/admin/users/%s/ban".formatted(user.getId()))
			.content("""
				{
					"reason": "기분이 나빠서"
				}""")
			.contentType("application/json")
			.characterEncoding("utf-8")
			.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken")));

		String id = objectMapper.readTree(perform.andReturn().getResponse().getContentAsString())
			.get("data").get("banListId").asText();

		BanList banList = banListRepository.findById(id).get();

		// then

		Optional<User> foundedUser = userService.getUserByUsername("user1");

		perform
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("banUser"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("유저 정지 성공"))
			.andExpect(jsonPath("$.data.banListId").value(banList.getId()))
			.andExpect(jsonPath("$.data.reason").value(banList.getReason()))
			.andExpect(jsonPath("$.data.userId").value(banList.getBannedUser().getId()))
			.andExpect(jsonPath("$.data.banCount").value(foundedUser.get().getBlockedCount()));
		Assertions.assertThat(foundedUser.get().getBlocked()).isTrue();
		Assertions.assertThat(foundedUser.get().getBlockedCount()).isEqualTo(1);
	}

	@Test
	void deletePost() throws Exception {
		//given
		// 로그인
		Map<String, Cookie> cookieMap = login("user4", "user41234@");

		//when
		// id를 가진 post를 삭제

		String id = productPostService.getPosts(1, 1, "", "asc")
			.getItems().get(0).getId();

		ResultActions result = mockMvc.perform(delete("/api/admin/posts/%s".formatted(id))
			.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken")));

		// then

		// 삭제된 product post 조회 시 오류 발생해야 한다.

		Assertions.assertThatThrownBy(
			() -> productPostService.getPost(id)
		).isInstanceOf(ProductPostNotFoundException.class);

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("204-1"))
			.andExpect(jsonPath("$.message").value("게시글 삭제 성공."));

	}
}