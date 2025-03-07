package com.NBE_4_5_2.Team5.domain.post.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.post.comment.dto.CommentDto;
import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment;
import com.NBE_4_5_2.Team5.domain.post.comment.repository.CommentRepository;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final UserService userService;
	private final ProductPostRepository productPostRepository;
	private final CommentRepository commentRepository;

	@Transactional
	public CommentDto writeComment(String postId, String content) {
		User loggedInUser = getUser();

		ProductPost productPost = productPostRepository.findById(postId)
			.orElseThrow(() -> new ServiceException("400-1", "id가 %s인 product post는 없습니다.".formatted(postId)));

		Comment comment = new Comment(content, productPost, loggedInUser);

		Comment saved = commentRepository.save(comment);
		return CommentDto.of(saved);
	}

	private User getUser() {
		return userService.getUserIdentity();
	}

	@Transactional
	public CommentDto updateComment(String commentId, String content) {
		User loggedInUser = getUser();

		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("id가 %s인 comment를 찾을 수 없습니다.".formatted(commentId)));

		comment.isMine(loggedInUser);

		comment.update(content);

		return CommentDto.of(comment);
	}

	public void deleteComment(String commentId) {
		User loggedInUser = getUser();

		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new EntityNotFoundException("id가 %s인 comment를 찾을 수 없습니다.".formatted(commentId)));

		comment.isMine(loggedInUser);

		commentRepository.delete(comment);
	}
}
