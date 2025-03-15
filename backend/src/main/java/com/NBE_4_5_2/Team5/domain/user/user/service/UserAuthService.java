package com.NBE_4_5_2.Team5.domain.user.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotValidException;
import com.NBE_4_5_2.Team5.global.security.SecurityUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserAuthService {
	private final UserService userService;

	public void setLogin(User actor) {

		UserDetails user = new SecurityUser(actor.getId(), actor.getUsername(), "", "", actor.getRole(), List.of());

		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
		);

	}

	public User getRealActor(User actor) {
		Optional<User> userById = userService.getUserById(actor.getId());
		assert userById.isPresent();
		return userById.get();
	}

	public User getUserIdentity() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		/**
		 * Spring Security에서는 인증되지 않은 사용자를 자동으로 `AnonymousAuthenticationToken`으로 설정
		 * 따라서 `authentication == null`이 아닐 수 있으므로 추가적인 확인을 진행함
		 */
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			throw new AuthenticationNotValidException("401-1", "로그인이 필요합니다.");
		}

		Object principal = authentication.getPrincipal();

		if (!(principal instanceof SecurityUser)) {
			throw new AuthenticationNotValidException("401-3", "잘못된 인증 정보입니다");
		}

		SecurityUser user = (SecurityUser)principal;

		return User.builder()
			.id(user.getId())
			.username(user.getUsername())
			.nickname(user.getNickname())
			.role(user.getRole())
			.build();
	}

	public UserDto getMe() {
		return new UserDto(getRealActor(getUserIdentity()));
	}
}
