package com.NBE_4_5_2.Team5.domain.post.post.dto.request;

import java.util.List;

import org.springframework.lang.NonNull;

public record ProductPostWriteForm(
	@NonNull String productName,
	@NonNull Integer productPrice,
	@NonNull String title,
	@NonNull String content,
	@NonNull List<Long> categoryIds,
	@NonNull List<String> imageUrlList,
	@NonNull Float latitude,
	@NonNull Float longitude
) {
}
