package com.NBE_4_5_2.Team5.domain.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentDto;
import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentMetaData;
import com.NBE_4_5_2.Team5.domain.payment.entity.Payment;
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;
import com.NBE_4_5_2.Team5.domain.payment.repository.PaymentRepository;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.exception.product.ProductNotFoundException;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final ProductPostRepository productRepository;
	// TODO : custom annotation으로 다른 PG사의 Adapter bean을 구분해서 가져오게 한다면?
	private final PaymentProviderAdapter paymentProviderAdapter;
	private final UserService userService;
	private final UserRepository userRepository;

	public PaymentMetaData saveMetaData(String paymentId, Integer amount) {
		User user = getLoggedInUser();
		Payment saved = paymentRepository.save(Payment.builder()
			.buyer(user)
			.id(paymentId)
			.totalPrice(amount)
			.status(PaymentStatus.IN_PROGRESS)
			.build());
		return new PaymentMetaData(saved);
	}

	private User getLoggedInUser() {
		return userService.getUserIdentity();
	}

	public PaymentDto purchase(String productId) {

		User loggedInUser = getLoggedInUser();

		User loggedInUserEntity = userRepository.findById(loggedInUser.getId()).get();
		ProductPost product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException("id가 %s인 Payment를 찾을 수 없습니다.".formatted(productId)));

		// 할인 등 product 가격과 총 결제 금액이 다를 수 있으므로 amount를 따로 받음.
		loggedInUserEntity.canBuy(product, product.getProductPrice());
		loggedInUserEntity.buy(product, product.getProductPrice());

		Payment purchasedPayment = Payment.builder()
			.id("payment-" + UUID.randomUUID())
			.buyer(loggedInUser)
			.totalPrice(-1 * product.getProductPrice())
			.status(PaymentStatus.DONE)
			.build();

		purchasedPayment.updatePaymentKey(productId);

		Payment saved = paymentRepository.save(purchasedPayment);

		return PaymentDto.of(saved);
	}

	public void requestCharge(@NotNull String id, @NotNull String paymentKey, @NotNull Integer amount) {

		User loggedInUser = getLoggedInUser();

		User loggedInUserEntity = userRepository.findById(loggedInUser.getId()).get();
		Payment payment = paymentRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Payment를 찾을 수 없습니다."));

		if (!payment.checkValid(amount)) {
			throw new IllegalArgumentException("총 가격이 맞지 않습니다.");
		}

		Payment update = payment.updatePaymentKey(paymentKey);

		try {
			paymentProviderAdapter.requestPayment(id, paymentKey, amount);

			loggedInUserEntity.chargeCash(amount);

			payment.updateState(PaymentStatus.DONE);

		} catch (RuntimeException e) {
			log.error(e.getMessage());
		}
	}

}
