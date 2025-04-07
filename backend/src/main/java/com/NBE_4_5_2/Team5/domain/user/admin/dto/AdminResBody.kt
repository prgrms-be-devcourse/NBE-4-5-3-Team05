package com.NBE_4_5_2.Team5.domain.user.admin.dto;

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;

public record AdminResBody(String id, String nickname, Role role) {
	public AdminResBody(User admin) {
		this(admin.getId(), admin.getNickname(), admin.getRole());
	}
}
