package com.NBE_4_5_2.Team5.domain.post.post.controller;

import com.NBE_4_5_2.Team5.domain.post.post.entity.LikedPost;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.LikedPostRepository;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.NBE_4_5_2.Team5.global.init.BaseInitData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductPostController 관련 기능(판매내역, 찜내역, 구매내역)을 통합 테스트하는 예시 코드
 */
@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductPostControllerTest  {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private UserService userService;

	@Autowired
	private ProductPostService productPostService;

	@Autowired
	private ProductPostRepository productPostRepository;

	@Autowired
	private LikedPostRepository likedPostRepository; // 찜 기능 테스트 시 필요

	@Autowired
	private BaseInitData baseInitData;

	private User seller;
	private User buyer;
	private String sellerToken;
	private String buyerToken;

	// @BeforeAll
	// void setup2() {
	// 	baseInitData.userInit();
	// 	baseInitData.categoryInit();
	// 	baseInitData.postInit();
	// 	baseInitData.noticeInit();
	// }

	@BeforeEach
	void beforeEach() {
		// 테스트용 유저 2명 사용
		seller = userService.getUserByUsername("user1").orElseThrow(
			() -> new RuntimeException("User not found")
		);
		buyer = userService.getUserByUsername("user2").orElseThrow(() -> new RuntimeException("User not found"));

		// JWT 토큰 발급
		sellerToken = userService.generateAuthTokenAsString(seller);
		buyerToken = userService.generateAuthTokenAsString(buyer);
	}

	@Test
	@DisplayName("내 판매 내역 조회 - 배열 어디엔가 내 글이 있는지 확인")
	void testGetMySales() throws Exception {
		// GIVEN: seller가 작성한 게시글 생성
		ProductPost post = createPost(seller, "테스트 판매 상품");
		String newPostId = post.getId(); // 예: "ppost-xxxxx..."

		// WHEN: seller로 /api/posts/my/sales 호출
		ResultActions resultActions = mvc.perform(
			get("/api/posts/my/sales")
				.header("Authorization", "Bearer " + sellerToken)
		).andDo(print());

		// THEN: 순서(배열 0번 등)가 아니라 "어디든 해당 ID가 포함"되었는지 확인
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("내 판매 내역 조회 성공"))
			// → data 배열 안의 모든 요소(id 필드) 중에 newPostId가 있으면 OK
			.andExpect(jsonPath("$.data[*].id").value(hasItem(newPostId)));
	}

	@Test
	@DisplayName("내가 찜한 내역 조회")
	void testGetMyFavorites() throws Exception {
		// GIVEN: seller가 작성한 게시글
		ProductPost post = createPost(seller, "찜 테스트 상품");

		// buyer가 이 게시글을 찜한 상태로 만들기
		LikedPost likedPost = LikedPost.builder()
			.userId(buyer.getId())
			.productPostId(post.getId())
			.build();
		likedPostRepository.save(likedPost);

		// WHEN: buyer 토큰으로 /api/posts/my/favorites 호출
		ResultActions resultActions = mvc.perform(
			get("/api/posts/my/favorites")
				.header("Authorization", "Bearer " + buyerToken)
		).andDo(print());

		// THEN
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("내가 찜한 내역 조회 성공"))
			.andExpect(jsonPath("$.data.items[0].id").value(post.getId()))
			.andExpect(jsonPath("$.data.items[0].writerId").value(seller.getId()));
	}

	@Test
	@DisplayName("내가 구매한 내역 조회")
	void testGetMyPurchases() throws Exception {
		// GIVEN: seller가 작성한 게시글
		ProductPost post = createPost(seller, "구매 테스트 상품");

		// buyer가 이 글을 구매한 것으로 처리
		productPostService.purchasePost(buyer, post.getId());

		// WHEN: buyer 토큰으로 /api/posts/my/purchases 호출
		ResultActions resultActions = mvc.perform(
			get("/api/posts/my/purchases")
				.header("Authorization", "Bearer " + buyerToken)
		).andDo(print());

		// THEN
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("내 구매 내역 조회 성공"))
			.andExpect(jsonPath("data.items[0].id").value(post.getId()))
			.andExpect(jsonPath("$.data.items[0].writerId").value(seller.getId()));
	}

	// ---------------------------
	// 테스트용 게시글 생성 메서드
	private ProductPost createPost(User writer, String title) {
		ProductPost post = ProductPost.create(
			writer,
			"상품명",
			10000,
			title,
			"테스트 내용",
			"https://example.com/1.jpg",
			37.5f,
			127.0f
		);
		return productPostRepository.save(post);
	}
}
