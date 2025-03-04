package com.NBE_4_5_2.Team5.domain.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.domain.admin.entity.BanList;
import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.admin.repository.BanListRepository;
import com.NBE_4_5_2.Team5.domain.admin.repository.NoticePostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import config.TestConfig;
import util.Util;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
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
	private BanListRepository banListRepository;
	@Autowired
	private Util util;

	@BeforeEach
	void setUp() throws Exception {
		util.truncateAllTables();
	}

	@Test
	void writeNotice() throws Exception {
		//given
		User admin = userRepository.save(new User("username", "password", "email", "nickname", "address",
			"url", Role.ADMIN, LocalDateTime.now(), LocalDateTime.now()));
		ResultActions perform = mockMvc.perform(post("/api/admin/notices")
			.content("""
				{
					"title": "공지 제목",
					"content": "공지 내용"
				}""")
			.contentType("application/json")
			.characterEncoding("utf-8"));

		String id = objectMapper.readTree(perform.andReturn().getResponse().getContentAsString())
			.get("data").get("id").asText();

		NoticePost noticePost = noticePostRepository.findById(id).get();

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

	@Test
	void banUser() throws Exception {
		//given
		User admin = userRepository.save(new User("username", "password", "email", "nickname", "address",
			"url", Role.ADMIN, LocalDateTime.now(), LocalDateTime.now()));
		User user = userRepository.save(new User("user1", "password", "email", "nickname", "address",
			"url", Role.USER, LocalDateTime.now(), LocalDateTime.now()));
		ResultActions perform = mockMvc.perform(post("/api/admin/users/%s/ban".formatted(user.getId()))
			.content("""
				{
					"reason": "기분이 나빠서"
				}""")
			.contentType("application/json")
			.characterEncoding("utf-8"));

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
		Assertions.assertThat(foundedUser.get().isBlocked()).isTrue();
		Assertions.assertThat(foundedUser.get().getBlockedCount()).isEqualTo(user.getBlockedCount() + 1);
	}
}