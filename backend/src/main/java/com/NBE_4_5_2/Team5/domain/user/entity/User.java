package com.NBE_4_5_2.Team5.domain.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "member")
public class User {

	@Id
	@Column(length = 255)
	private String id = "user-" + UUID.randomUUID();  // user-UUID

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String nickname;

	private String address;

	@Column(name = "profile_url")
	private String profileUrl;

	@Enumerated(EnumType.STRING)
	private Role role; // 0: admin , 1: 일반 유저

	private boolean blocked;
	private int blockedCount;

	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public User(String username, String password, String email, String nickname, String address, String profileUrl,
		Role role,
		LocalDateTime createdAt, LocalDateTime modifiedAt) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.blocked = false;
		this.blockedCount = 0;
		this.nickname = nickname;
		this.address = address;
		this.profileUrl = profileUrl;
		this.role = role;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

}
