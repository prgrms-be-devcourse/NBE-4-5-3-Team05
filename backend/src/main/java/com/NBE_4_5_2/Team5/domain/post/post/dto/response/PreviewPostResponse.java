package com.NBE_4_5_2.Team5.domain.post.post.dto.response;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;

import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PreviewPostResponse {
    private String id;
    private String productName;
    private Integer productPrice;
    private String title;
    private String writerId;
    private String writerName;
    private Float latitude;
    private Float longitude;
    private String thumbNail;
    private LocalDateTime createdAt;
    private String imageUrls;
    private Integer viewCount;
    private Integer likedCount;
    private ProductStatus status;

    public static PreviewPostResponse fromEntity(ProductPost post) {
        return PreviewPostResponse.builder()
                .id(post.getId())
                .productName(post.getProductName())
                .productPrice(post.getProductPrice())
                .title(post.getTitle())
                .writerId(post.getWriter().getId())
                .writerName(post.getWriter().getNickname())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .thumbNail(post.imageUrls.split(",")[0])
                .imageUrls(post.imageUrls)
                .createdAt(post.createdDate)
                .viewCount(post.getViewCount())
                .likedCount(0)
                .status(post.getStatus())
                .build();
    }
    public static PreviewPostResponse fromEntityWithLikeCount(ProductPost post, int likedCount) {
        return PreviewPostResponse.builder()
                .id(post.getId())
                .productName(post.getProductName())
                .productPrice(post.getProductPrice())
                .title(post.getTitle())
                .writerId(post.getWriter().getId())
                .writerName(post.getWriter().getNickname())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .thumbNail(post.imageUrls != null && !post.imageUrls.trim().isEmpty()
                        ? post.imageUrls.split(",")[0]
                        : "")
                .createdAt(post.createdDate)
                .viewCount(post.getViewCount())
                .likedCount(likedCount)
                .status(post.getStatus())
                .build();
    }


}

