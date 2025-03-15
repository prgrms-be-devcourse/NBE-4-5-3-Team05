package com.NBE_4_5_2.Team5.domain.post.post.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;

public record ProductPostModifyForm(
	@Parameter(name = "상품 이름")
	String productName,
	@Parameter(name = "상품 가격")
	Integer productPrice,
	@Parameter(name = "상품 게시글 제목")
	String title,
	@Parameter(name = "상품 게시글 내용")
	String content,
	@Parameter(name = "상품 카테고리")
	List<Long> categoryIds,
	@Parameter(name = "상품 이미지 url")
	List<String> imageUrlList,
	@Parameter(name = "위도")
	Float latitude,
	@Parameter(name = "경도")
	Float longitude
) {
}
