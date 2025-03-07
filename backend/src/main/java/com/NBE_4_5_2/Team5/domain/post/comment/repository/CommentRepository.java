package com.NBE_4_5_2.Team5.domain.post.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, String> {
}