package com.NBE_4_5_2.Team5.domain.product.dto;

import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductDto {
    private String id;
    private String productName;
    private int productPrice;
    private String title;
    private String content;
    private int likedCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ProductDto fromEntity(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .title(product.getTitle())
                .content(product.getContent())
                .likedCount(product.getLikedCount())
                .createdAt(product.getCreatedAt())
                .modifiedAt(product.getModifiedAt())
                .build();

    }

}
