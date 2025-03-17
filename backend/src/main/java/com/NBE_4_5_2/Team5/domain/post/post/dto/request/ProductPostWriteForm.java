package com.NBE_4_5_2.Team5.domain.post.post.dto.request;

import java.util.List;

import org.springframework.lang.NonNull;

import io.swagger.v3.oas.annotations.Parameter;

public record ProductPostWriteForm(
	@Parameter(description = "상품 이름")
	@NonNull String productName,
	@Parameter(description = "상품 가격")
	@NonNull Integer productPrice,
	@Parameter(description = "글 제목")
	@NonNull String title,
	@Parameter(description = "글 내용")
	@NonNull String content,
	@Parameter(description = "카테고리 id")
	@NonNull List<Long> categoryIds,
	@Parameter(description = "상품 사진 url")
	@NonNull List<String> imageUrlList,
	@Parameter(description = "위도")
	@NonNull Float latitude,
	@Parameter(description = "경도")
	@NonNull Float longitude
) {
}
