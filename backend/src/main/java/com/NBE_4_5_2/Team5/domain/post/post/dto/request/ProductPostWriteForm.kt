package com.NBE_4_5_2.Team5.domain.post.post.dto.request;

import io.swagger.v3.oas.annotations.Parameter;

data class ProductPostWriteForm(
	@Parameter(description = "상품 이름")
	val productName: String,

	@Parameter(description = "상품 가격")
	val productPrice: Int,

	@Parameter(description = "글 제목")
	val title: String,

	@Parameter(description = "글 내용")
	val content: String,

	@Parameter(description = "카테고리 id")
	val categoryIds: List<Long>,

	@Parameter(description = "상품 사진 url")
	val imageUrlList: List<String>,

	@Parameter(description = "위도")
	val latitude: Float,

	@Parameter(description = "경도")
	val longitude: Float,

	@Parameter(description = "거래 위치(주소)")
	val location: String
)
