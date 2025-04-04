package com.NBE_4_5_2.Team5.infrastructure.toss;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.NBE_4_5_2.Team5.domain.payment.service.PaymentProviderAdapter;
import com.NBE_4_5_2.Team5.global.exception.TossPaymentException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TossPaymentProviderAdapter implements PaymentProviderAdapter {

	@Value("${custom.toss.payment.secret}")
	private String tossPaymentSecretKey;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class TossPaymentReqBody {
		private String paymentKey;
		private String orderId;
		private int amount;
	}

	@Override
	public ResponseEntity<Map<String, Object>> requestPayment(String id, String paymentKey, Integer amount) {

		RestClient restClient = RestClient.create();
		ResponseEntity<Map> response = restClient.post()
			.uri("https://api.tosspayments.com/v1/payments/confirm")
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic " + tossPaymentSecretKey)
			.body(new TossPaymentReqBody(paymentKey, id, amount))
			.retrieve()
			.toEntity(Map.class);

		if (!response.getStatusCode().equals(HttpStatus.OK) || !response.hasBody()) {
			throw new TossPaymentException("결제 승인 요청에 실패했습니다.");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> body = (Map<String, Object>)response.getBody();
		assert body != null;
		return new ResponseEntity<>(body, response.getStatusCode());
	}
}
