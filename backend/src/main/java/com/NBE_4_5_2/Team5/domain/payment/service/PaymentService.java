package com.NBE_4_5_2.Team5.domain.payment.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.member.repository.MemberRepository;
import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentDto;
import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentMetaData;
import com.NBE_4_5_2.Team5.domain.payment.entity.Payment;
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;
import com.NBE_4_5_2.Team5.domain.payment.repository.PaymentRepository;
import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import com.NBE_4_5_2.Team5.domain.product.repository.ProductRepository;
import com.NBE_4_5_2.Team5.global.exception.TossPaymentException;
import com.NBE_4_5_2.Team5.global.exception.product.ProductNotFoundException;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final MemberRepository memberRepository;
	private final ProductRepository productRepository;

	@Value("${custom.toss.payment.secret}")
	private String tossPaymentSecretKey;

	public PaymentMetaData saveMetaData(String paymentId, Integer amount) {
		Member user = getUser();
		Payment saved = paymentRepository.save(Payment.builder()
			.buyer(user)
			.id(paymentId)
			.totalPrice(amount)
			.status(PaymentStatus.IN_PROGRESS)
			.build());
		return new PaymentMetaData(saved);
	}

	private Member getUser() {
		return memberRepository.findAll().get(0);
	}

	public PaymentDto purchase(String productId, Integer amount) {

		Member loggedInUser = getUser();
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException("id가 %s인 Payment를 찾을 수 없습니다.".formatted(productId)));

		// 할인 등 product 가격과 총 결제 금액이 다를 수 있으므로 amount를 따로 받음.
		loggedInUser.canBuy(product, amount);
		loggedInUser.buy(product, amount);

		Payment purchasedPayment = Payment.builder()
			.id("payment-" + UUID.randomUUID())
			.buyer(loggedInUser)
			.totalPrice(-1 * amount)
			.status(PaymentStatus.DONE)
			.build();

		Payment saved = paymentRepository.save(purchasedPayment);

		return PaymentDto.of(saved);
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class TossPaymentReqBody {
		private String paymentKey;
		private String orderId;
		private int amount;
	}

	public void requestCharge(@NotNull String id, @NotNull String paymentKey, @NotNull Integer amount) {
		Member loggedInUser = getUser();
		Payment payment = paymentRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Payment를 찾을 수 없습니다."));

		if (!payment.checkValid(amount)) {
			throw new IllegalArgumentException("총 가격이 맞지 않습니다.");
		}

		Payment update = payment.updatePaymentKey(paymentKey);

		try {
			RestClient restClient = RestClient.create();
			ResponseEntity<Map> response = restClient.post()
				.uri("https://api.tosspayments.com/v1/payments/confirm")
				.header("Content-Type", "application/json")
				.header("Authorization", "Basic " + tossPaymentSecretKey)
				.body(new TossPaymentReqBody(update.getPaymentKey(), update.getId(), update.getTotalPrice()))
				.retrieve()
				.toEntity(Map.class);

			if (!response.getStatusCode().equals(HttpStatus.OK) || !response.hasBody()) {
				throw new TossPaymentException("결제 승인 요청에 실패했습니다.");
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> body = (Map<String, Object>)response.getBody();
			assert body != null;
			Integer totalAmount = (Integer)body.get("totalAmount");

			loggedInUser.chargeCash(totalAmount);

			payment.updateState(PaymentStatus.DONE);

		} catch (RuntimeException e) {
			log.error(e.getMessage());
		}
	}
}
