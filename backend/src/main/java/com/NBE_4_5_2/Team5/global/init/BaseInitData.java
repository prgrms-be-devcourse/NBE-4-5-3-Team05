package com.NBE_4_5_2.Team5.global.init;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.NBE_4_5_2.Team5.domain.product.dto.ProductStatus;
import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import com.NBE_4_5_2.Team5.domain.product.repository.ProductRepository;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
	private final UserService userService;
	private final ProductRepository productRepository;

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			userInit();
			productInit();
		};
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

	}

	@org.springframework.transaction.annotation.Transactional
	public void productInit() {
		if (productRepository.count() > 0) {
			return;
		}

		productRepository.save(new Product(ProductStatus.AVAILABLE, 5000));
	}
}