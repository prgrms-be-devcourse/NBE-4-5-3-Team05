package com.NBE_4_5_2.Team5.domain.payment.controller;

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

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@GetMapping("/metadata")
	public RsData<PaymentMetaData> saveMetaData(@NotNull @RequestParam String id,
		@NotNull @RequestParam Integer amount) {
		PaymentMetaData metadata = paymentService.saveMetaData(id, amount);
		return new RsData<>("201-1", "결제 메타데이터 저장 성공.", metadata);
	}

	@GetMapping("/request")
	public RsData<Void> requestPayment(@RequestParam(name = "orderId") @NotNull String id,
		@RequestParam(name = "paymentKey") @NotNull String paymentKey,
		@RequestParam(name = "amount") @NotNull Integer amount) {
		paymentService.requestCharge(id, paymentKey, amount);

		return new RsData<>("200-1", "페이 충전 결제 요청 성공.");
	}

	public record PurchaseItemReqDto(String productId) {
	}

	@PostMapping("")
	public RsData<PaymentDto> purchaseItem(@RequestBody @NotNull PurchaseItemReqDto reqBody) {
		PaymentDto purchased = paymentService.purchase(reqBody.productId());

		return new RsData<>("200-1", "상품 구매 성공.", purchased);
	}
}
