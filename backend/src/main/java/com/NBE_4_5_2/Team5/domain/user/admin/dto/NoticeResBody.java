package com.NBE_4_5_2.Team5.domain.user.admin.dto;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.user.admin.entity.NoticePost;

public record NoticeResBody(String id, String title, String content, AdminResBody admin, LocalDateTime createdAt) {
	public static NoticeResBody of(NoticePost saved) {
		return new NoticeResBody(saved.getId(), saved.getTitle(), saved.getContent(),
			new AdminResBody(saved.getAdmin()), saved.getCreatedAt());
	}
}
