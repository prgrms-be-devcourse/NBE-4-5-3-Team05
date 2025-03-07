package com.NBE_4_5_2.Team5.domain.admin.dto;

import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;

public record NoticeResBody(String id, String title, String content, AdminResBody admin) {
	public static NoticeResBody of(NoticePost saved) {
		return new NoticeResBody(saved.getId(), saved.getTitle(), saved.getContent(),
			new AdminResBody(saved.getAdmin()));
	}
}
