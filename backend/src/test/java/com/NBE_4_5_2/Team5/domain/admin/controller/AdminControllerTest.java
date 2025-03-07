package com.NBE_4_5_2.Team5.domain.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.TestConfig;
import com.NBE_4_5_2.Team5.Util;
import com.NBE_4_5_2.Team5.domain.admin.entity.BanList;
import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.admin.repository.BanListRepository;
import com.NBE_4_5_2.Team5.domain.admin.repository.NoticePostRepository;
import com.NBE_4_5_2.Team5.domain.admin.service.AdminService;
import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Order(-100)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private NoticePostRepository noticePostRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private BanListRepository banListRepository;
	@Autowired
	private ProductPostRepository productPostRepository;
	@Autowired
	private Util util;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private AdminService adminService;

	@BeforeEach
	public void setUp() throws Exception {
		util.truncateAllTables();
	}

	@Test
	void writeNotice() throws Exception {
		//given
		User admin = userRepository.save(
			User.builder()
				.id("user-" + UUID.randomUUID())
				.username("username")
				.password(passwordEncoder.encode("password"))
				.email("email")
				.nickname("nickname")
				.address("address")
				.refreshToken(UUID.randomUUID().toString())
				.role(Role.ADMIN)
				.profileUrl("url")
				.build());
		Map<String, Cookie> cookieMap = login(admin.getUsername(), "password");

		// when
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

		NoticePost noticePost = noticePostRepository.findById(id).get();

		// then
		perform
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("writeNotice"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("공지사항 등록 성공."))
			.andExpect(jsonPath("$.data.id").value(noticePost.getId()))
			.andExpect(jsonPath("$.data.title").value(noticePost.getTitle()))
			.andExpect(jsonPath("$.data.content").value(noticePost.getContent()))
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
		User admin = userRepository.save(
			User.builder()
				.id("user-" + UUID.randomUUID())
				.username("admin")
				.password(passwordEncoder.encode("password"))
				.email("email1@email.com")
				.nickname("nickname1")
				.address("address")
				.refreshToken(UUID.randomUUID().toString())
				.role(Role.ADMIN)
				.profileUrl("url")
				.build());
		User user = userRepository.save(User.builder()
			.id("user-" + UUID.randomUUID())
			.username("username")
			.password(passwordEncoder.encode("password"))
			.email("email2@email.com")
			.nickname("nickname2")
			.address("address")
			.profileUrl("url")
			.role(Role.USER)
			.refreshToken(UUID.randomUUID().toString())
			.build());

		Map<String, Cookie> cookieMap = login(admin.getUsername(), "password");
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
		Optional<User> foundedUser = userRepository.findById(user.getId());

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
		Assertions.assertThat(foundedUser.get().getBlockedCount()).isEqualTo(user.getBlockedCount() + 1);
	}

	@Test
	void deletePost() throws Exception {
		//given
		Category category = categoryRepository.save(Category.builder().name("cat1").build());
		User user = adminService.signUpAdmin("user1", "password", "email");
		ProductPost post = productPostRepository.save(
			ProductPost.create(user, "name", 5000, "title", "content", "url", 30F, 40F)
		);

		Map<String, Cookie> cookieMap = login("user1", "password");

		//when
		mockMvc.perform(delete("/api/admin/posts/%s".formatted(post.getId()))
				.cookie(cookieMap.get("accessToken"), cookieMap.get("refreshToken")))
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("deletePost"));

	}
}