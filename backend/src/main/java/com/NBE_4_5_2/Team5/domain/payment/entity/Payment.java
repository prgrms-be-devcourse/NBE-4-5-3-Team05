package com.NBE_4_5_2.Team5.domain.payment.entity;

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime;
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Payment extends BaseTime {

	@Id
	@Builder.Default
	@EqualsAndHashCode.Include
	@Column(updatable = false, nullable = false)
	private String id = "payment-" + UUID.randomUUID();

	private String paymentKey;

	@ManyToOne
	private User buyer;

	private int totalPrice;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	public Payment(String id, User buyer, int totalPrice, PaymentStatus status) {
		this.id = id;
		this.buyer = buyer;
		this.totalPrice = totalPrice;
		this.status = status;
	}

	/**
	 * 결제 상태를 업데이트합니다.
	 * @param status {@link PaymentStatus} 결제 상태 enum
	 */
	public void updateState(PaymentStatus status) {
		this.status = status;
	}

	/**
	 * 결제 요청 전에 저장한 payment 메타데이터({@link com.NBE_4_5_2.Team5.domain.payment.dto.PaymentMetaData PaymentMetaData}
	 * )의 {@code totalAmount}와 인자로 들어오는 {@code amount}가 같은지 비교합니다.
	 * @param amount 결제 요청시 클라이언트가 전달한 총 지불 금액
	 * @return 요청 전에 저장한 데이터와 인자로 받은 총 가격이 동일하다면 {@code true}, 그렇지 않다면 {@code false}를 반환합니다.
	 */
	public boolean checkValid(@NotNull Integer amount) {
		return totalPrice == amount;
	}

	public Payment updatePaymentKey(@NotNull String paymentKey) {
		this.paymentKey = paymentKey;
		return this;
	}
}
