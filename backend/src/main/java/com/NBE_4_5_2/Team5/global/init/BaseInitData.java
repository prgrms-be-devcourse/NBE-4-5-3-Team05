package com.NBE_4_5_2.Team5.global.init;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.NBE_4_5_2.Team5.domain.admin.service.AdminService;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class BaseInitData {
	private final UserService userService;
	private final UserRepository userRepository;
	private final AdminService adminService;

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> userInit();
	}

	@Transactional
	public void userInit() {

		if (userService.count() > 0) {
			return;
		}

		userService.signup("user1", "user11234@", "user1@gmail.com", "user1", "서울시 강남구",
			"https://example.com/default_profile.png");
		userService.signup("user2", "user21234@", "user2@gmail.com", "user2", "서울시 강서구",
			"https://example.com/default_profile.png");
		userService.signup("user3", "user31234@", "user3@gmail.com", "user3", "서울시 광진구",
			"https://example.com/default_profile.png");

		adminService.signUpAdmin("admin", "password", "admin@gmail.com");

	}
}