package com.NBE_4_5_2.Team5.domain.post.comment.entity;

import java.util.UUID;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Comment {

	@Id
	private final String id = "comment-" + UUID.randomUUID();

	private String content;

	@ManyToOne
	private ProductPost target;

	@ManyToOne
	private User author;

	@Builder
	public Comment(String content, ProductPost target, User author) {
		this.content = content;
		this.target = target;
		target.addComment(this);
		this.author = author;
		author.addWroteComments(this);
	}
}
