package com.NBE_4_5_2.Team5.domain.post.comment.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.post.comment.dto.CommentDto;
import com.NBE_4_5_2.Team5.domain.post.comment.service.CommentService;
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Comment API", description = "댓글 API")
public class PostCommentController {

	private final CommentService commentService;

	public record WriteCommentResBody(String id, String content, UserDto author) {
	}

	public record WriteCommentReqBody(
		@Parameter(description = "댓글 내용") String content) {
	}

	@Operation(summary = "댓글 작성", description = "상품에 댓글을 작성합니다.")
	@PostMapping("/{post-id}/comments")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	public RsData<WriteCommentResBody> writeComment(
		@Parameter(description = "작성할 상품 게시글 id")
		@PathVariable(name = "post-id") String postId,
		@RequestBody WriteCommentReqBody body) {

		CommentDto commentDto = commentService.writeComment(postId, body.content());

		return new RsData<>("200-1", "댓글 작성 성공.",
			new WriteCommentResBody(commentDto.getId(), commentDto.getContent(), commentDto.getAuthor()));

	}

	public record UpdateCommentResBody(String content, UserDto author) {
	}

	public record UpdateCommentReqBody(String content) {
	}

	@Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@PutMapping("/{post-id}/comments/{comment-id}")
	public RsData<UpdateCommentResBody> updateComment(@PathVariable(name = "comment-id") String commentId,
		@RequestBody UpdateCommentReqBody body) {

		CommentDto commentDto = commentService.updateComment(commentId, body.content);

		return new RsData<>("200-1", "comment 수정 성공.",
			new UpdateCommentResBody(commentDto.getContent(), commentDto.getAuthor()));
	}

	@Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@DeleteMapping("/{post-id}/comments/{comment-id}")
	public RsData<Void> deleteComment(@PathVariable(name = "comment-id") String commentId) {
		commentService.deleteComment(commentId);

		return new RsData<>("204-1", "comment 삭제 성공.");
	}

	@GetMapping("/{id}/comments")
	public RsData<Slice<CommentDto>> getComments(@PathVariable(name = "id") String postId, Pageable pageable) {
		Slice<CommentDto> comments = commentService.getComments(postId, pageable);

		return new RsData<>("200-1", "comment 조회 성공", comments);
	}
}
