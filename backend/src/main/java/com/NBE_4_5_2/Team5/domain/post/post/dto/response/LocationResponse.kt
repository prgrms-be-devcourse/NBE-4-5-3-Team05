package com.NBE_4_5_2.Team5.domain.post.post.dto.response;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;

import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;

data class LocationResponse(
        val id: String,
        val productName: String,
        val productPrice: Int,
        val title: String,
        val writerId: String,
        val writerName: String,
        val latitude: Float,
        val longitude: Float,
        val thumbNail: String,
        val createdAt: LocalDateTime,
        val imageUrls: String,
        val viewCount: Int,
        val likedCount: Int,
        val status: ProductStatus,
        val distance: Double
) {

    companion object {

        fun fromEntity(post: ProductPost): LocationResponse {
            return LocationResponse(
                    id = post.id,
                    productName = post.productName,
                    productPrice = post.productPrice,
                    title = post.title,
                    writerId = post.writer.id,
                    writerName = post.writer.nickname,
                    latitude = post.latitude,
                    longitude = post.longitude,
                    thumbNail = post.imageUrls.split(",").firstOrNull().orEmpty(),
                    createdAt = post.createdDate,
                    imageUrls = post.imageUrls,
                    viewCount = post.viewCount,
                    likedCount = 0,
                    status = post.status,
                    distance = post.distance
            )
        }


        fun fromEntityWithLikeCount(post: ProductPost, likedCount: Int): LocationResponse {
            val thumbNail = post.imageUrls
                    .takeIf { it.isNotBlank() }
                ?.split(",")
                    ?.firstOrNull()
                    .orEmpty()

            return LocationResponse(
                    id = post.id,
                    productName = post.productName,
                    productPrice = post.productPrice,
                    title = post.title,
                    writerId = post.writer.id,
                    writerName = post.writer.nickname,
                    latitude = post.latitude,
                    longitude = post.longitude,
                    thumbNail = thumbNail,
                    createdAt = post.createdDate,
                    imageUrls = post.imageUrls,
                    viewCount = post.viewCount,
                    likedCount = likedCount,
                    status = post.status,
                    distance = post.distance
            )
        }
    }
}

