package com.NBE_4_5_2.Team5.domain.post.post.dto.response

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import java.time.LocalDateTime

class ProductPostResponse(
    val id: String,
    val writerId: String,
    val writerName: String,
    val productName: String,
    val productPrice: Int,
    val title: String,
    val content: String,
    val imageUrls: String,
    val latitude: Float,
    val longitude: Float,
    val location: String, // 거래위치 (추가)
    val categories: List<String>,
    val createdAt: LocalDateTime, // 생성일
    val modifiedAt: LocalDateTime, // 수정일
    val viewCount: Int, // 조회수
    val likedCount: Int,
    val status: ProductStatus
) {

    companion object {
        
        fun fromEntity(post: ProductPost): ProductPostResponse {
            return ProductPostResponse(
                post.id,
                post.writer.id,
                post.writer.nickname,
                post.productName,
                post.productPrice,
                post.title,
                post.content,
                post.imageUrls,
                post.latitude,
                post.longitude,
                post.location,
                post.productCategories.map { it.category?.name ?: "" },
                post.createdDate,
                post.modifiedDate,
                post.viewCount,
                0,
                post.status
            )
        }

        // 찜 개수를 외부에서 전달받는 메서드
        
        fun fromEntityWithLikeCount(post: ProductPost, likedCount: Int): ProductPostResponse {
            return ProductPostResponse(
                post.id,
                post.writer.id,
                post.writer.nickname,
                post.productName,
                post.productPrice,
                post.title,
                post.content,
                post.imageUrls,
                post.latitude,
                post.longitude,
                post.location,
                post.productCategories.map { it.category?.name ?: "" },
                post.createdDate,
                post.modifiedDate,
                post.viewCount,
                likedCount,
                post.status
            )
        }
    }
}
