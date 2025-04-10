package com.NBE_4_5_2.Team5.domain.post.post.dto.request;


import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import io.swagger.v3.oas.annotations.Parameter;

data class ProductPostModifyForm(
    @Parameter(name = "상품 이름")
    val productName: String? = null,

    @Parameter(name = "상품 가격")
    val productPrice: Int? = null,

    @Parameter(name = "상품 게시글 제목")
    val title: String? = null,

    @Parameter(name = "상품 게시글 내용")
    val content: String? = null,

    @Parameter(name = "상품 카테고리")
    val categoryIds: List<Long>? = null,

    @Parameter(name = "상품 이미지 url")
    val imageUrlList: List<String>? = null,

    @Parameter(name = "위도")
    val latitude: Float? = null,

    @Parameter(name = "경도")
    val longitude: Float? = null,

    @Parameter(name = "상품 상태")
    val status: ProductStatus? = null,

    @Parameter(name = "거래 위치(주소)")
    val location: String? = null
)