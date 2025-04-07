package com.NBE_4_5_2.Team5.domain.user.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeDto {

	private String id;
	private String title;
	private String content;
	private String authorId;

	@Builder
	public NoticeDto(String id, String title, String content, String authorId) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.authorId = authorId;
	}
}
