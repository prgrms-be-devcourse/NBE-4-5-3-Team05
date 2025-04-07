package com.NBE_4_5_2.Team5.domain.payment.dto;

import com.NBE_4_5_2.Team5.domain.payment.entity.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class PaymentMetaData {
	private String paymentId;
	private Integer amount;

	public PaymentMetaData(Payment payment) {
		this.paymentId = payment.getId();
		this.amount = payment.getTotalPrice();
	}
}
