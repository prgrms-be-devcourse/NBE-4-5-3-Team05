package com.NBE_4_5_2.Team5.domain.payment.dto

import com.NBE_4_5_2.Team5.domain.payment.entity.Payment
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

class PaymentMetaData(
    var paymentId: String,
    var amount: Int
) {
    constructor(payment: Payment) : this(payment.id, payment.totalPrice)
}
