package com.NBE_4_5_2.Team5.domain.payment.dto;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.payment.entity.Payment;
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;

public record PaymentDto(
	String id, String buyerId, int totalPrice, LocalDateTime createdAt, LocalDateTime modifiedAt,
	PaymentStatus status) {
	public static PaymentDto of(Payment payment) {
		return new PaymentDto(payment.getId(), payment.getBuyer().getId(), payment.getTotalPrice(),
			payment.getCreatedAt(), payment.getModifiedAt(), payment.getStatus());
	}
}
