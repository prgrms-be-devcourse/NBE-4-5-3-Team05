package com.NBE_4_5_2.Team5.global.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.NBE_4_5_2.Team5.domain.user.entity.Role;

import lombok.Getter;

public class SecurityUser extends User {

	@Getter
	private String id;
	@Getter
	private Role role;

	public SecurityUser(String id, String username, String password,
		Collection<? extends GrantedAuthority> authorities, Role role) {
		super(username, password, authorities);
		this.id = id;
		this.role = role;
	}
}
