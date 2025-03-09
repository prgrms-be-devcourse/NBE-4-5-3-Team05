package com.NBE_4_5_2.Team5.domain.post.comment.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.post.comment.dto.CommentDto;
import com.NBE_4_5_2.Team5.domain.post.comment.service.CommentService;
import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostCommentController {

	private final CommentService commentService;

	public record WriteCommentResBody(String id, String content, UserDto author) {
	}

	public record WriteCommentReqBody(String content) {
	}

	@PostMapping("/{post-id}/comments")
	public RsData<WriteCommentResBody> writeComment(@PathVariable(name = "post-id") String postId,
		@RequestBody WriteCommentReqBody body) {

		CommentDto commentDto = commentService.writeComment(postId, body.content());

		return new RsData<>("200-1", "댓글 작성 성공.",
			new WriteCommentResBody(commentDto.getId(), commentDto.getContent(), commentDto.getAuthor()));

	}

	public record UpdateCommentResBody(String content, UserDto author) {
	}

	public record UpdateCommentReqBody(String content) {
	}

	@PutMapping("/{post-id}/comments/{comment-id}")
	public RsData<UpdateCommentResBody> updateComment(@PathVariable(name = "comment-id") String commentId,
		@RequestBody UpdateCommentReqBody body) {

		CommentDto commentDto = commentService.updateComment(commentId, body.content);

		return new RsData<>("200-1", "comment 수정 성공.",
			new UpdateCommentResBody(commentDto.getContent(), commentDto.getAuthor()));
	}

	@DeleteMapping("/{post-id}/comments/{comment-id}")
	public RsData<Void> deleteComment(@PathVariable(name = "comment-id") String commentId) {
		commentService.deleteComment(commentId);

		return new RsData<>("204-1", "comment 삭제 성공.");
	}
}
