package com.NBE_4_5_2.Team5.domain.post.comment.dto;

import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentDto {

	private String id;
	private UserDto author;
	private String content;
	private String postId;

	private CommentDto(String id, UserDto author, String content, String postId) {
		this.id = id;
		this.author = author;
		this.content = content;
		this.postId = postId;
	}

	public static CommentDto of(Comment comment) {
		return new CommentDto(comment.getId(), new UserDto(comment.getAuthor()), comment.getContent(),
			comment.getTarget().getId());
	}
}
