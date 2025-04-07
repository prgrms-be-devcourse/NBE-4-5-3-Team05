package com.NBE_4_5_2.Team5.domain.payment.dto

import com.NBE_4_5_2.Team5.domain.payment.entity.Payment
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
class PaymentMetaData(payment: Payment) {
    private var paymentId = payment.id
    private var amount = payment.totalPrice
}
