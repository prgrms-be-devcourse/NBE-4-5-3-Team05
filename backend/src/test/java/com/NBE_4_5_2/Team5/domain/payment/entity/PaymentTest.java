package com.NBE_4_5_2.Team5.domain.payment.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;

class PaymentTest {

	@Test
	void updateStateTest() {
		Payment payment = new Payment();
		payment.updateState(PaymentStatus.DONE);

		Assertions.assertThat(payment.getStatus()).isEqualByComparingTo(PaymentStatus.DONE);
	}

	@Test
	void checkValid() {
		Payment payment = new Payment();
		Integer metadataValue = 50000;
		ReflectionTestUtils.setField(payment, "totalPrice", metadataValue);

		Integer clientValue = 50000;

		boolean res = payment.checkValid(clientValue);

		Assertions.assertThat(res).isTrue();
	}
}