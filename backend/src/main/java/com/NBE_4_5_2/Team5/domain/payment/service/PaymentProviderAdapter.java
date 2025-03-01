package com.NBE_4_5_2.Team5.domain.payment.service;

public interface PaymentProviderAdapter {
	void requestPayment(String id, String paymentKey, Integer amount);
}
