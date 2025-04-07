package com.NBE_4_5_2.Team5.domain.payment.dto

import com.NBE_4_5_2.Team5.domain.payment.entity.Payment
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus
import java.time.LocalDateTime

@JvmRecord
data class PaymentDto(
    val id: String,
    val buyerId: String,
    val totalPrice: Int,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val status: PaymentStatus?
) {
    companion object {
        fun of(payment: Payment): PaymentDto {
            return PaymentDto(
                payment.id, payment.buyer.id, payment.totalPrice,
                payment.createdDate, payment.modifiedDate, payment.status
            )
        }
    }
}
