package com.NBE_4_5_2.Team5.domain.user.user.dto;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

	private String id;
	private String username;
	private String email;
	private String nickname;
	private String address;
	private String profileUrl;
	private Role role;
	private int cash;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	private boolean blocked;
	private int blockedCount;

	public UserDto(User admin) {
		this.id = admin.getId();
		this.role = admin.getRole();
		this.username = admin.getUsername();
		this.email = admin.getEmail();
		this.nickname = admin.getNickname();
		this.address = admin.getAddress();
		this.profileUrl = admin.getProfileUrl();
		this.blocked = admin.getBlocked();
		this.cash = admin.getCash();
		this.blockedCount = admin.getBlockedCount();
		this.createdAt = admin.getCreatedAt();
		this.modifiedAt = admin.getModifiedAt();
	}

	public static UserDto fromEntity(User user) {
		return new UserDto(user);
	}
}
