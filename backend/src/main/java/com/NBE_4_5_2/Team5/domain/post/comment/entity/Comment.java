package com.NBE_4_5_2.Team5.domain.post.comment.entity;

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Comment extends BaseTime {

	@Id
	@Builder.Default
	@EqualsAndHashCode.Include
	@Column(updatable = false, nullable = false)
	private String id = "comment-" + UUID.randomUUID();

	@ManyToOne
	private ProductPost target;

	@ManyToOne
	private User author;

	private String content;

	public Comment(String content, ProductPost target, User author) {
		this.content = content;
		this.target = target;
		this.author = author;
		target.addComment(this);
		author.addWroteComments(this);
	}

	public void isMine(User loggedInUser) {
		if (!this.author.equals(loggedInUser)) {
			throw new ForbiddenAccessException("403-1", "작성자가 아닙니다.");
		}
	}

	public void update(String content) {
		this.content = content;
	}
}
