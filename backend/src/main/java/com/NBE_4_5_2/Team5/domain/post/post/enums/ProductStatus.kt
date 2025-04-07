package com.NBE_4_5_2.Team5.domain.post.post.enums

enum class ProductStatus(private val description: String) {
    RESERVED("예약중"),
    AVAILABLE("판매중"),
    PURCHASED("판매 완료")
}
