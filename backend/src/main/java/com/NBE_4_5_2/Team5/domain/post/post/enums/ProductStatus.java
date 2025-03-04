package com.NBE_4_5_2.Team5.domain.post.post.enums;

public enum ProductStatus {
    RESERVED("예약중"),
    AVAILABLE("판매중"),
    PURCHASED("판매 완료");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }
}
