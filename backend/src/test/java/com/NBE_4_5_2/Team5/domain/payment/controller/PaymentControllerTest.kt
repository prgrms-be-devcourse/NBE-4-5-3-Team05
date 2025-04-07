package com.NBE_4_5_2.Team5.domain.payment.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;
import com.NBE_4_5_2.Team5.domain.payment.service.PaymentProviderAdapter;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.PreviewPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.NBE_4_5_2.Team5.global.config.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

@BaseTestConfig
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentControllerTest {

	@MockitoBean
	private PaymentProviderAdapter paymentProvider;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ProductPostService productPostService;

	private Cookie accessTokenCookie;
	private Cookie refreshTokenCookie;

	@Autowired
	private Util util;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserService userService;

	@BeforeAll
	void setUp() throws Exception {
		// 로그인하여 accessToken, refreshToken 저장
		MockHttpServletResponse response = mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						\"username\":\"%s\",
						\"password\":\"%s\"
					}""".formatted("user1", "user11234@")))
			.andReturn().getResponse();

		accessTokenCookie = response.getCookie("accessToken");
		refreshTokenCookie = response.getCookie("refreshToken");
	}

	@Test
	@DisplayName("결제 요청 전 총 결제 가격과 주문 id를 저장해야 한다.")
	void saveMetaData() throws Exception {
		// given

		// 주문 시작 전 클라이언트에서 생성한 랜덤한 "order-"+uuid 형식의 주문 id
		String uuid = "order-" + UUID.randomUUID();

		// 주문 시작 전 유저가 선택한 총 결제 가격
		Integer totalPrice = 12000;

		//when
		// 주문 id와 총 결제 가격을 쿼리파라미터로 전달.
		ResultActions perform = mockMvc.perform(get("/api/payments/metadata?id=%s&amount=%s"
			.formatted(uuid, totalPrice))
			.contentType(MediaType.APPLICATION_JSON)
			.cookie(accessTokenCookie, refreshTokenCookie));

		//then
		// response body로 전달된 값이 제대로 들어왔는지 확인

		perform.andExpect(status().isCreated());
		perform.andExpect(jsonPath("$.code").value("201-1"));
		perform.andExpect(jsonPath("$.message").value("결제 메타데이터 저장 성공."));
		perform.andExpect(jsonPath("$.data.paymentId").value(uuid));
		perform.andExpect(jsonPath("$.data.amount").value(totalPrice));
	}

	@Test
	@DisplayName("결제 승인 테스트")
	void requestPaymentTest() throws Exception {
		// given
		// 유저가 클라이언트에서 결제 시작 -> 결제 메타데이터 백엔드 저장 -> 유저가 클라이언트에서 PG사에 결제 요청 request
		// -> 결제 요청 response에 paymentKey 포함 -> 클라이언트에서 paymentKey를 포함한 요청을 백엔드에 전송
		// -> 백엔드는 paymentKey, orderId, totalPayment로 결제 승인 request

		// 결제 승인 이전 결제 요청 진행
		// 주문 시작 전 클라이언트에서 생성한 랜덤한 "order-"+uuid 형식의 주문 id
		String uuid = "order-" + UUID.randomUUID();

		// 주문 시작 전 유저가 선택한 총 결제 가격
		Integer totalPrice = 12000;

		// 주문 id와 총 결제 가격을 쿼리파라미터로 전달.
		ResultActions perform = mockMvc.perform(get("/api/payments/metadata?id=%s&amount=%s"
			.formatted(uuid, totalPrice))
			.contentType(MediaType.APPLICATION_JSON)
			.cookie(accessTokenCookie, refreshTokenCookie));

		perform
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("201-1"));

		// 결제 요청 성공 이후 클라이언트로부터 PG사에서 생성한 paymentKey를 포함한 요청을 받음.
		// 랜덤하고 유일한 값이므로 UUID로 대체
		String paymentKey = UUID.randomUUID().toString();

		// when
		// 결제 요청 request 성공 이후 결제 승인 request 처리
		// 결제 승인 처리 과정에서 PG사의 API를 호출해야 함. 이를 모킹
		Map<String, Object> body = util.paymentRequestResponse(uuid, paymentKey, totalPrice);

		Mockito.when(paymentProvider.requestPayment(uuid, paymentKey, totalPrice))
			.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		ResultActions result = mockMvc.perform(get("/api/payments/request?orderId=%s&paymentKey=%s&amount=%s"
			.formatted(uuid, paymentKey, totalPrice))
			.cookie(accessTokenCookie, refreshTokenCookie)
		);

		// then

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.code").value("200-1"));
		result.andExpect(jsonPath("$.message").value("페이 충전 결제 요청 성공."));
	}

	@SuppressWarnings("checkstyle:WhitespaceAround")
	@Test
	@DisplayName("페이머니로 상품 구매")
	void payProduct() throws Exception {
		// given
		// 구매할 상품 찾기
		PreviewPostResponse product = productPostService.getPosts(1, 1, "", "asc")
			.getItems().get(0);

		// 유저의 캐시가 차감되었는지 확인하기 위해 유저 엔티티 가져옴
		User userBeforeBuy = userService.getUserByUsername("user1")
			.orElseThrow(() -> new RuntimeException());
		int beforeCash = userBeforeBuy.getCash();

		// 상품 가격만큼 캐시 충전
		chargeCash(product.getProductPrice());

		// when

		ResultActions response = mockMvc.perform(post("/api/payments")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
					\"productId\": \"%s\"
				}
				""".formatted(product.getId()))
			.cookie(accessTokenCookie, refreshTokenCookie));

		// then

		// 유저가 상품 가격만큼 결제하고 구매했으므로 총 캐시는 이전과 동일해야 함(beforecash)

		User user1 = userService.getUserByUsername("user1")
			.orElseThrow(() -> new RuntimeException());

		response.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("상품 구매 성공."))
			.andExpect(jsonPath("$.data.totalPrice").value(product.getProductPrice() * -1))
			.andExpect(jsonPath("$.data.status").value(PaymentStatus.DONE.toString()));

		assertThat(user1.getCash()).isEqualTo(beforeCash);
	}

	@Test
	@DisplayName("상품 구매 여부 조회")
	void isPurchased() throws Exception {
		//given

		// 상품을 로그인한 유저가 구매

		// 구매할 상품 찾기
		PreviewPostResponse product = productPostService.getPosts(1, 1, "", "asc")
			.getItems().get(0);

		// 유저의 캐시가 차감되었는지 확인하기 위해 유저 엔티티 가져옴
		User userBeforeBuy = userService.getUserByUsername("user1")
			.orElseThrow(() -> new RuntimeException());
		int beforeCash = userBeforeBuy.getCash();

		// 상품 가격만큼 캐시 충전
		chargeCash(product.getProductPrice());

		// 상품 구매
		ResultActions response = mockMvc.perform(post("/api/payments")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
					\"productId\": \"%s\"
				}
				""".formatted(product.getId()))
			.cookie(accessTokenCookie, refreshTokenCookie));

		response
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200-1"));

		// when
		// 상품을 구매한 유저가 로그인하고 API를 호출

		ResultActions result = mockMvc.perform(get("/api/payments?post-id=%s"
			.formatted(product.getId()))
			.cookie(accessTokenCookie, refreshTokenCookie));

		// then
		// 결제했으므로 true를 반환해야함.
		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.message").value("상품 결제 여부 조회 성공."))
			.andExpect(jsonPath("$.data").value(true));

	}

	private void chargeCash(int totalAmount) throws Exception {
		String uuid = "order-" + UUID.randomUUID();

		// 주문 시작 전 유저가 선택한 총 결제 가격
		Integer totalPrice = totalAmount;

		// 주문 id와 총 결제 가격을 쿼리파라미터로 전달.
		ResultActions perform = mockMvc.perform(get("/api/payments/metadata?id=%s&amount=%s"
			.formatted(uuid, totalPrice))
			.contentType(MediaType.APPLICATION_JSON)
			.cookie(accessTokenCookie, refreshTokenCookie));

		perform
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("201-1"));

		// 결제 요청 성공 이후 클라이언트로부터 PG사에서 생성한 paymentKey를 포함한 요청을 받음.
		// 랜덤하고 유일한 값이므로 UUID로 대체
		String paymentKey = UUID.randomUUID().toString();

		// when

		// 결제 승인 처리 과정에서 PG사의 API를 호출해야 함. 이를 모킹
		Map<String, Object> body = util.paymentRequestResponse(uuid, paymentKey, totalPrice);

		Mockito.when(paymentProvider.requestPayment(uuid, paymentKey, totalPrice))
			.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		// 결제 요청 request 성공 이후 결제 승인 request 처리
		ResultActions result = mockMvc.perform(get("/api/payments/request?orderId=%s&paymentKey=%s&amount=%s"
			.formatted(uuid, paymentKey, totalPrice))
			.cookie(accessTokenCookie, refreshTokenCookie)
		);

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200-1"));
	}
}