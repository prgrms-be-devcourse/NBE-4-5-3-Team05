package com.NBE_4_5_2.Team5.domain.payment.enums

enum class PaymentStatus(private val description: String) {
    IN_PROGRESS("진행중"), DONE("완료됨"), ABORTED("취소됨")
}
