package com.NBE_4_5_2.Team5.domain.admin.dto;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NoticeDto {
	private final String id;
	private final String title;
	private final String content;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final UserDto admin;

	public static NoticeDto of(NoticePost noticePost) {
		return new NoticeDto(noticePost.getId(), noticePost.getTitle(), noticePost.getContent(),
			noticePost.getCreatedAt(),
			noticePost.getModifiedAt(), new UserDto(noticePost.getAdmin())
		);
	}
}
