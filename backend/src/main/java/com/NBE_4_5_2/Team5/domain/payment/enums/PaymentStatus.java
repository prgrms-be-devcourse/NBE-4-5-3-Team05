package com.NBE_4_5_2.Team5.domain.payment.enums;

public enum PaymentStatus {
	IN_PROGRESS("진행중"), DONE("완료됨"), ABORTED("취소됨");
	private final String description;

	PaymentStatus(String description) {
		this.description = description;
	}
}
