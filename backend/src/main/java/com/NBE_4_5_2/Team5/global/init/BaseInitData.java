package com.NBE_4_5_2.Team5.global.init;

import java.util.ArrayList;
import java.util.List;

import com.NBE_4_5_2.Team5.domain.admin.entity.NoticePost;
import com.NBE_4_5_2.Team5.domain.admin.repository.NoticePostRepository;
import com.NBE_4_5_2.Team5.domain.admin.service.AdminService;
import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductCategory;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductCategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
	private final CategoryRepository categoryRepository;
	private final ProductPostRepository postRepository;
	private final ProductCategoryRepository productCategoryRepository;
	private final UserService userService;
	private final UserRepository userRepository;
	private final AdminService adminService;
	private final NoticePostRepository noticePostRepository;

	@Autowired
	@Lazy
	private BaseInitData self;

	@Bean
	@Order(1)
	public ApplicationRunner applicationRunner1() {
		return args -> {
			self.userInit();
		};
	}

	@Bean
	@Order(2)
	public ApplicationRunner applicationRunner2() {
		return args -> {
			self.categoryInit();
		};
	}

	@Bean
	@Order(3)
	public ApplicationRunner applicationRunner3() {
		return args -> {
			self.postInit();
		};
	}

	@Bean
	@Order(4)
	public ApplicationRunner applicationRunner4() {
		return args -> {
			self.noticeInit();
		};
	}

	@Transactional
	public void userInit() {

		if (userService.count() > 0) {
			return;
		}

		userService.createUser("user1", "user11234@", "user1@gmail.com", "user1", "서울시 강남구",
			"https://example.com/default_profile.png");
		userService.createUser("user2", "user21234@", "user2@gmail.com", "user2", "서울시 강서구",
			"https://example.com/default_profile.png");
		userService.createUser("user3", "user31234@", "user3@gmail.com", "user3", "서울시 광진구",
			"https://example.com/default_profile.png");

		adminService.signUpAdmin("user4", "user41234@", "user4@gmail.com");

	}

	@Transactional
	public void categoryInit() {
		if (categoryRepository.count() > 0) {
			return; // 이미 카테고리가 존재하면 초기화하지 않음
		}

		List<Category> categories = List.of(
			new Category(null, "전자제품"),
			new Category(null, "가구"),
			new Category(null, "의류"),
			new Category(null, "스포츠 용품"),
			new Category(null, "도서"),
			new Category(null, "생활용품"),
			new Category(null, "자동차 용품"),
			new Category(null, "식품"),
			new Category(null, "악기"),
			new Category(null, "반려동물 용품"),
			new Category(null, "뷰티/미용"),
			new Category(null, "티켓/쿠폰"),
			new Category(null, "수집/예술"),
			new Category(null, "게임"),
			new Category(null, "기타")
		);

		categoryRepository.saveAll(categories);
	}

	@Transactional
	public void postInit() {
		if (postRepository.count() > 0) {
			return;
		}

		List<User> users = userRepository.findAll();
		System.out.println(users.size());
		List<Category> categories = categoryRepository.findAll();

		List<ProductPost> posts = new ArrayList<>();

		// 0,1,2
		for (int i = 1; i <= 50; i++) {
			User writer = users.get((i - 1) % users.size());
			posts.add(ProductPost.create(
				writer,
				"상품 " + i,
				(i * 10000) % 200000 + 10000, // 가격 랜덤화
				"제목 " + i,
				"이것은 테스트 상품 " + i + " 입니다.",
				"https://example.com/product" + i + "_1.jpg,https://example.com/product" + i + "_2.jpg",
				37.5f + (i % 10) * 0.01f, // 위치 랜덤화
				127.0f + (i % 10) * 0.01f
			));
		}

		postRepository.saveAll(posts);

		// ✅ `ProductCategory` 생성하여 게시글과 랜덤 카테고리 연결
		List<ProductCategory> productCategories = new ArrayList<>();

		for (int i = 0; i < posts.size(); i++) {
			Category randomCategory = categories.get(i % categories.size()); // ✅ 순환하면서 랜덤 카테고리 적용
			productCategories.add(ProductCategory.builder()
				.productPost(posts.get(i))
				.category(randomCategory)
				.build());
		}

		productCategoryRepository.saveAll(productCategories);
	}

	@Transactional
	public void noticeInit() {
		if (noticePostRepository.count() > 0) {
			return;
		}

		// 공지사항 생성: 총 10개의 공지사항 생성
		User admin = userRepository.findAll().stream()
				.filter(u -> u.getRole().equals(Role.ADMIN))
				.findFirst()
				.orElse(null);
		if (admin == null && !userRepository.findAll().isEmpty()) {
			admin = userRepository.findAll().get(0);
		}

		for (int i = 1; i <= 10; i++) {
			NoticePost notice = NoticePost.builder()
					.admin(admin)
					.title("공지사항 제목 " + i)
					.content("공지사항 내용 " + i + " - 중요한 공지사항 내용입니다.")
					.build();
			noticePostRepository.save(notice);
		}
	}
}
