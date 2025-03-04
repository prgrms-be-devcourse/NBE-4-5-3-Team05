package com.NBE_4_5_2.Team5.global.init;

import java.time.LocalDateTime;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {

	private final UserRepository userRepository;

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> userInit();
	}

	@Transactional
	public void userInit() {

		if (userRepository.count() > 0) {
			return;
		}

		userRepository.save(
			new User("admin", "password", "email", "nickname", "address", "profileUrl", Role.ADMIN, LocalDateTime.now(),
				LocalDateTime.now()));
	}
}