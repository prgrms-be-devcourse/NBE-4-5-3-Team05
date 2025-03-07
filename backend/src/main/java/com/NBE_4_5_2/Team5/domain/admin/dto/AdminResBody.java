package com.NBE_4_5_2.Team5.domain.admin.dto;

import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;

public record AdminResBody(String id, String nickname, Role role) {
	public AdminResBody(User admin) {
		this(admin.getId(), admin.getNickname(), admin.getRole());
	}
}
