package com.NBE_4_5_2.Team5.domain.user.entity;

import java.util.UUID;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.NBE_4_5_2.Team5.global.entity.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
public class User extends BaseTime {

	@Id
	private String id = "user-" + UUID.randomUUID();

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
	@Enumerated(EnumType.ORDINAL)
	private Role role; // 0: admin , 1: 일반 유저

	@Column(nullable = false)
	private boolean blocked;

	@Column(name = "blocked_count", nullable = false)
	@Builder.Default
	private Integer blockedCount = 0;

	public User(String username, String password, String email, String nickname, String address, String profileUrl,
		Role role) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.blocked = false;
		this.blockedCount = 0;
		this.nickname = nickname;
		this.address = address;
		this.profileUrl = profileUrl;
		this.role = role;
	}

	public boolean isAdmin() {
		return this.role.equals(Role.ADMIN);
	}

	public void ban() {
		this.blocked = true;
		this.blockedCount++;
	}

	public void unBan() {

		if (!this.blocked) {
			return;
		}
		blocked = false;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
