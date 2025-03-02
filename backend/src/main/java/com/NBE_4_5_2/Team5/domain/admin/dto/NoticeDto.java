package com.NBE_4_5_2.Team5.domain.admin.dto;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoticeDto {
	private String id;
	private String title;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private UserDto admin;

	public static NoticeDto of(NoticePost noticePost) {
		return new NoticeDto(noticePost.getId(), noticePost.getTitle(), noticePost.getContent(),
			noticePost.getCreatedAt(),
			noticePost.getModifiedAt(), new UserDto(noticePost.getAdmin())
		);
	}
}
