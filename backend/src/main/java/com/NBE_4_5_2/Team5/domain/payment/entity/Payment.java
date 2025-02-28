package com.NBE_4_5_2.Team5.domain.payment.entity;

import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    private String buyerId;

    private int totalPrice;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    private PaymentStatus status;

}
