package com.NBE_4_5_2.Team5.domain.payment.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentDto;
import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentMetaData;
import com.NBE_4_5_2.Team5.domain.payment.service.PaymentService;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "결제 API")
public class PaymentController {

	private final PaymentService paymentService;

	@Operation(summary = "결제 메타데이터 저장", description = "결제흐름 진행 전 메타데이터를 저장합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/metadata")
	public RsData<PaymentMetaData> saveMetaData(
		@Parameter(description = "구매할 상품 게시글 id")
		@NotNull @RequestParam String id,
		@Parameter(description = "총 결제 금액")
		@NotNull @RequestParam Integer amount) {
		PaymentMetaData metadata = paymentService.saveMetaData(id, amount);
		return new RsData<>("201-1", "결제 메타데이터 저장 성공.", metadata);
	}

	@Operation(summary = "결제 승인", description = "PG사에 결제 승인을 요청합니다. PG사 서버로부터 리다이렉트된 요청을 받아 PG사로 결제 승인 API를 호출합니다.")
	@SecurityRequirement(name = "cookieAuth")
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/request")
	public RsData<Void> requestPayment(
		@Parameter(description = "결제 id")
		@RequestParam(name = "orderId") @NotNull String id,
		@Parameter(description = "PG사에서 생성한 paymentKey")
		@RequestParam(name = "paymentKey") @NotNull String paymentKey,
		@Parameter(description = "총 구매 가격")
		@RequestParam(name = "amount") @NotNull Integer amount) {
		paymentService.requestCharge(id, paymentKey, amount);

		return new RsData<>("200-1", "페이 충전 결제 요청 성공.");
	}

	public record PurchaseItemReqDto(
		@Parameter(description = "구매할 상품 게시글 id")
		String productId) {
	}

	@Operation(summary = "상품 구매", description = "페이머니로 상품을 구매합니다.")
	@PreAuthorize("isAuthenticated()")
	@PostMapping("")
	public RsData<PaymentDto> purchaseItem(
		@RequestBody @NotNull PurchaseItemReqDto reqBody) {
		PaymentDto purchased = paymentService.purchase(reqBody.productId());

		return new RsData<>("200-1", "상품 구매 성공.", purchased);
	}

	@Operation(summary = "상품 구매 여부 조회", description = "로그인한 유저가 상품을 구매했는지 여부를 반환합니다.")
	@PreAuthorize("isAuthenticated()")
	@GetMapping
	public RsData<Boolean> checkPurchased(@RequestParam(name = "post-id") String postId) {
		Boolean isPurchased = paymentService.isPurchased(postId);

		return new RsData<>("200-1", "상품 결제 여부 조회 성공.", isPurchased);
	}

}
