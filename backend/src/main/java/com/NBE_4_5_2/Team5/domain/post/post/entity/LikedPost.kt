package com.NBE_4_5_2.Team5.domain.post.post.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseLongIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "liked_post")
class LikedPost(
    @Column(nullable = false)
    val userId: String,  // 찜한 사용자 ID

    @Column(nullable = false)
    val productPostId: String // 찜한 상품 ID
) : BaseLongIdEntity() {

    companion object {
        @JvmStatic
        fun of(userId: String, productPostId: String): LikedPost {
            return LikedPost(userId, productPostId)
        }
    }
}
