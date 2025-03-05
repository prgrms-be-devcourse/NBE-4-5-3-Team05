package com.NBE_4_5_2.Team5.domain.payment.service;

/**
 * 결제를 위한 API 호출을 포함한 PG사에 맞는 세부적인 로직이 포함된 클래스
 */
public interface PaymentProviderAdapter {
	/**
	 * 결제 승인 절차를 진행하기 위해 외부 API를 호출한다.
	 * @param id
	 * @param paymentKey
	 * @param amount
	 */
	void requestPayment(String id, String paymentKey, Integer amount);
}
