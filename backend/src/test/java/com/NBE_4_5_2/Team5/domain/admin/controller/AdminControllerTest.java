package com.NBE_4_5_2.Team5.domain.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.admin.repository.NoticePostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private NoticePostRepository noticePostRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ObjectMapper objectMapper;

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
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("공지사항 등록 성공."))
			.andExpect(jsonPath("$.data.id").value(noticePost.getId()))
			.andExpect(jsonPath("$.data.title").value(noticePost.getTitle()))
			.andExpect(jsonPath("$.data.content").value(noticePost.getContent()))
			.andExpect(jsonPath("$.data.admin.id").value(admin.getId()));
	}
}