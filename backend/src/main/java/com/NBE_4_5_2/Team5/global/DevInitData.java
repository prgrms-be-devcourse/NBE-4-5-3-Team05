package com.NBE_4_5_2.Team5.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.member.repository.MemberRepository;
import com.NBE_4_5_2.Team5.domain.product.dto.ProductStatus;
import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import com.NBE_4_5_2.Team5.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

	private final MemberRepository memberRepository;

	@Lazy
	@Autowired
	private DevInitData self;
	@Autowired
	private ProductRepository productRepository;

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			self.memberInit();
			self.productInit();
		};
	}

	@Transactional
	public void memberInit() {
		if (memberRepository.count() > 0) {
			return;
		}

		memberRepository.save(new Member());
	}

	@Transactional
	public void productInit() {
		if (productRepository.count() > 0) {
			return;
		}

		productRepository.save(new Product(ProductStatus.AVAILABLE, 5000));
	}
}
