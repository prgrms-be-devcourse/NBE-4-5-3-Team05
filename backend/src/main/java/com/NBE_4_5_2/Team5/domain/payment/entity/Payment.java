package com.NBE_4_5_2.Team5.domain.payment.entity;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Payment {

    @Id
    private String id;
    private String paymentKey;

    @ManyToOne
    private Member buyer;

    private int totalPrice;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    private PaymentStatus status;

    @Builder
    public Payment(String id, Member buyer, int totalPrice, PaymentStatus status) {
        this.id = id;
        this.buyer = buyer;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    /**
     * 결제 상태를 업데이트합니다.
     * @param status
     */
    public void updateState(PaymentStatus status) {
        this.status = status;
    };

    /**
     * 결제 요청 전에 저장한 payment메타데이터의 {@code totalAmount}와 인자로 들어오는 {@code amount}가 같은지 비교합니다.
     * @param amount 결제 요청시 클라이언트가 전달한 총 지불 금액
     * @return 요청 전에 저장한 데이터와 인자로 받은 총 가격이 동일하다면 {@code true}, 그렇지 않다면 {@code false}를 반환합니다.
     */
    public boolean checkValid(@NotNull Integer amount) {
        return totalPrice == amount;
    }

    public Payment update(@NotNull String paymentKey) {
        this.paymentKey = paymentKey;
        return this;
    }
}
