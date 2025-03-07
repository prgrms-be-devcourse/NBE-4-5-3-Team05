package com.NBE_4_5_2.Team5.domain.post.post.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductPostResponse {
	private String id;
	private String writerId;
	private String writerName;
	private String productName;
	private Integer productPrice;
	private String title;
	private String content;
	private String imageUrls;
	private Float latitude;
	private Float longitude;
	private List<String> categories;

	public static ProductPostResponse fromEntity(ProductPost post) {
		return new ProductPostResponse(
			post.getId(),
			post.getWriter().getId(),
			post.getWriter().getNickname(),
			post.getProductName(),
			post.getProductPrice(),
			post.getTitle(),
			post.getContent(),
			post.getImage_urls(),
			post.getLatitude(),
			post.getLongitude(),
			post.getProductCategories().stream()
				.map(pc -> pc.getCategory().getName())
				.collect(Collectors.toList())
		);
	}
}
