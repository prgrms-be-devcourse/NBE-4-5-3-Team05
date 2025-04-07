package com.NBE_4_5_2.Team5.domain.post.post.dto.request;


import io.swagger.v3.oas.annotations.Parameter;

data class ProductPostModifyForm(
    @Parameter(name = "상품 이름")
    val productName: String,

    @Parameter(name = "상품 가격")
    val productPrice: Int,

    @Parameter(name = "상품 게시글 제목")
    val title: String,

    @Parameter(name = "상품 게시글 내용")
    val content: String,

    @Parameter(name = "상품 카테고리")
    val categoryIds: MutableList<Long>,

    @Parameter(name = "상품 이미지 url")
    val imageUrlList: MutableList<String>,

    @Parameter(name = "위도")
    val latitude: Float,

    @Parameter(name = "경도")
    val longitude: Float
) {
}
