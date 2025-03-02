package com.NBE_4_5_2.Team5.domain.user.dto;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
	private String id;
	private Role role; // 0 : admin 1: user
	private String username;
	private String email;
	private String nickname;
	private String address;
	private String profileUrl;
	private boolean blocked;
	private int blockedCount;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public UserDto(User admin) {
		this.id = admin.getId();
		this.role = admin.getRole();
		this.username = admin.getUsername();
		this.email = admin.getEmail();
		this.nickname = admin.getNickname();
		this.address = admin.getAddress();
		this.profileUrl = admin.getProfileUrl();
		this.blocked = admin.isBlocked();
		this.blockedCount = admin.getBlockedCount();
		this.createdAt = admin.getCreatedAt();
		this.modifiedAt = admin.getModifiedAt();
	}
}
