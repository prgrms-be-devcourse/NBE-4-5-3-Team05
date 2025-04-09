package com.NBE_4_5_2.Team5.domain.payment.controller

import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus
import com.NBE_4_5_2.Team5.domain.payment.service.PaymentProviderAdapter
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import util.Util
import java.util.*

@BaseTestConfig
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PaymentControllerTest(


    @Autowired
    private val mockMvc: MockMvc,

    @Autowired
    private val productPostService: ProductPostService,


    @Autowired
    private val util: Util,

    @Autowired
    private val userService: UserService,
) {
    @MockitoBean
    private lateinit var paymentProvider: PaymentProviderAdapter
    private lateinit var accessTokenCookie: Cookie
    private lateinit var refreshTokenCookie: Cookie

    @BeforeAll
    @Throws(Exception::class)
    fun setUp() {
        val username = "user1"
        val password = "user11234@"
        // 로그인하여 accessToken, refreshToken 저장
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
					{
						"username":"$username",
						"password":"$password"
					}
					""".trimIndent()
                )
        )
            .andReturn().response

        accessTokenCookie = response.getCookie("accessToken")!!
        refreshTokenCookie = response.getCookie("refreshToken")!!
    }

    @Test
    @DisplayName("결제 요청 전 총 결제 가격과 주문 id를 저장해야 한다.")
    @Throws(
        Exception::class
    )
    fun saveMetaData() {
        // given

        // 주문 시작 전 클라이언트에서 생성한 랜덤한 "order-"+uuid 형식의 주문 id

        val uuid = "order-" + UUID.randomUUID()

        // 주문 시작 전 유저가 선택한 총 결제 가격
        val totalPrice = 12000

        //when
        // 주문 id와 총 결제 가격을 쿼리파라미터로 전달.
        val perform = mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/payments/metadata?id=${uuid}&amount=${totalPrice}"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        //then
        // response body로 전달된 값이 제대로 들어왔는지 확인
        perform.andExpect(MockMvcResultMatchers.status().isCreated())
        perform.andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201-1"))
        perform.andExpect(MockMvcResultMatchers.jsonPath("$.message").value("결제 메타데이터 저장 성공."))
        perform.andExpect(MockMvcResultMatchers.jsonPath("$.data.paymentId").value(uuid))
        perform.andExpect(MockMvcResultMatchers.jsonPath("$.data.amount").value(totalPrice))
    }

    @Test
    @DisplayName("결제 승인 테스트")
    @Throws(Exception::class)
    fun requestPaymentTest() {
        // given
        // 유저가 클라이언트에서 결제 시작 -> 결제 메타데이터 백엔드 저장 -> 유저가 클라이언트에서 PG사에 결제 요청 request
        // -> 결제 요청 response에 paymentKey 포함 -> 클라이언트에서 paymentKey를 포함한 요청을 백엔드에 전송
        // -> 백엔드는 paymentKey, orderId, totalPayment로 결제 승인 request

        // 결제 승인 이전 결제 요청 진행
        // 주문 시작 전 클라이언트에서 생성한 랜덤한 "order-"+uuid 형식의 주문 id

        val uuid = "order-" + UUID.randomUUID()

        // 주문 시작 전 유저가 선택한 총 결제 가격
        val totalPrice = 12000

        // 주문 id와 총 결제 가격을 쿼리파라미터로 전달.
        val perform = mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/payments/metadata?id=${uuid}&amount=${totalPrice}"
            )
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        perform
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201-1"))

        // 결제 요청 성공 이후 클라이언트로부터 PG사에서 생성한 paymentKey를 포함한 요청을 받음.
        // 랜덤하고 유일한 값이므로 UUID로 대체
        val paymentKey = UUID.randomUUID().toString()

        // when
        // 결제 요청 request 성공 이후 결제 승인 request 처리
        // 결제 승인 처리 과정에서 PG사의 API를 호출해야 함. 이를 모킹
        val body = util.paymentRequestResponse(uuid, paymentKey, totalPrice)

        Mockito.`when`(paymentProvider.requestPayment(uuid, paymentKey, totalPrice))
            .thenReturn(ResponseEntity(body, HttpStatus.OK))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/payments/request?orderId=%s&paymentKey=%s&amount=%s"
                    .formatted(uuid, paymentKey, totalPrice)
            )
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
        result.andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
        result.andExpect(MockMvcResultMatchers.jsonPath("$.message").value("페이 충전 결제 요청 성공."))
    }

    @Test
    @DisplayName("페이머니로 상품 구매")
    @Throws(Exception::class)
    fun payProduct() {
        // given
        // 구매할 상품 찾기
        val product = productPostService.getPosts(1, 1, "", "asc", 0, 1000000, emptyList())
            .items[0]

        // 유저의 캐시가 차감되었는지 확인하기 위해 유저 엔티티 가져옴
        val userBeforeBuy = userService.getUserByUsername("user1")
            .orElseThrow { RuntimeException() }
        val beforeCash = userBeforeBuy.cash

        // 상품 가격만큼 캐시 충전
        chargeCash(product.productPrice)

        // when
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
				{
					${'"'}productId${'"'}: ${'"'}%s${'"'}
				}
				
				""".trimIndent().formatted(product.id)
                )
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        // then

        // 유저가 상품 가격만큼 결제하고 구매했으므로 총 캐시는 이전과 동일해야 함(beforecash)
        val user1 = userService.getUserByUsername("user1")
            .orElseThrow { RuntimeException() }

        response.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("상품 구매 성공."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPrice").value(product.productPrice * -1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value(PaymentStatus.DONE.toString()))

        Assertions.assertThat(user1.cash).isEqualTo(beforeCash)
    }

    @Throws(Exception::class)
    @DisplayName("상품 구매 여부 조회")
    @Test
    fun isPurchased() {
        //given

        // 상품을 로그인한 유저가 구매

        // 구매할 상품 찾기

        val product = productPostService.getPosts(1, 1, "", "asc", 0, 1000000, emptyList())
            .items[0]

        // 유저의 캐시가 차감되었는지 확인하기 위해 유저 엔티티 가져옴
        val userBeforeBuy = userService.getUserByUsername("user1")
            .orElseThrow { RuntimeException() }
        val beforeCash = userBeforeBuy.cash

        // 상품 가격만큼 캐시 충전
        chargeCash(product.productPrice)

        // 상품 구매
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
				{
					${'"'}productId${'"'}: ${'"'}%s${'"'}
				}
				
				""".trimIndent().formatted(product.id)
                )
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        response
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))

        // when
        // 상품을 구매한 유저가 로그인하고 API를 호출
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/payments?post-id=%s"
                    .formatted(product.id)
            )
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        // then
        // 결제했으므로 true를 반환해야함.
        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("상품 결제 여부 조회 성공."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(true))
    }

    @Throws(Exception::class)
    private fun chargeCash(totalAmount: Int) {
        val uuid = "order-" + UUID.randomUUID()

        // 주문 시작 전 유저가 선택한 총 결제 가격
        val totalPrice = totalAmount

        // 주문 id와 총 결제 가격을 쿼리파라미터로 전달.
        val perform = mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/payments/metadata?id=%s&amount=%s"
                    .formatted(uuid, totalPrice)
            )
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        perform
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201-1"))

        // 결제 요청 성공 이후 클라이언트로부터 PG사에서 생성한 paymentKey를 포함한 요청을 받음.
        // 랜덤하고 유일한 값이므로 UUID로 대체
        val paymentKey = UUID.randomUUID().toString()

        // when

        // 결제 승인 처리 과정에서 PG사의 API를 호출해야 함. 이를 모킹
        val body = util.paymentRequestResponse(uuid, paymentKey, totalPrice)

        Mockito.`when`(paymentProvider.requestPayment(uuid, paymentKey, totalPrice))
            .thenReturn(ResponseEntity(body, HttpStatus.OK))

        // 결제 요청 request 성공 이후 결제 승인 request 처리
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/payments/request?orderId=%s&paymentKey=%s&amount=%s"
                    .formatted(uuid, paymentKey, totalPrice)
            )
                .cookie(accessTokenCookie, refreshTokenCookie)
        )

        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
    }
}