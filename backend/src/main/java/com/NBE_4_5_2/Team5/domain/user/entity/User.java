package com.NBE_4_5_2.Team5.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment;
import com.NBE_4_5_2.Team5.global.entity.BaseTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
public class User extends BaseTime {

	@Column(length = 20, nullable = false, unique = true)
	private String username;

	@Column(length = 255, nullable = false)
	private String password;

	@Column(length = 100, unique = true)
	private String refreshToken;

	@Column(length = 50, nullable = false, unique = true)
	private String email;

	@Column(length = 20, nullable = false, unique = true)
	private String nickname;

	@Column(length = 255)
	private String address;

	@Column(name = "profile_url", length = 255)
	private String profileUrl;

	@Column(nullable = false)
	@Builder.Default
	private Integer role = 1;  // 0: Admin, 1: 일반 유저

	@Column(nullable = false)
	@Builder.Default
	private Boolean blocked = false;

	@Column(name = "blocked_count", nullable = false)
	@Builder.Default
	private Integer blockedCount = 0;

	@OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private final List<Comment> wroteComments = new ArrayList<>();

	public boolean isAdmin() {
		return this.role == 0;
	}

	public void addWroteComments(Comment comment) {
		wroteComments.add(comment);
	}
}
