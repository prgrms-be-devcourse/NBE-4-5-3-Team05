package com.NBE_4_5_2.Team5.domain.post.post.repository

import com.NBE_4_5_2.Team5.domain.post.post.entity.LikedPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LikedPostRepository : JpaRepository<LikedPost, Long> {
    // 특정 유저가 특정 게시글에 찜한 기록이 있는지 체크
    fun existsByUserIdAndProductPostId(userId: String, productPostId: String): Boolean

    // 특정 게시글에 찜한 개수를 조회
    fun countByProductPostId(productPostId: String): Int

    // 기존 기능 (내가 찜한 내역 조회용)
    @Query("SELECT lp.productPostId FROM LikedPost lp WHERE lp.userId = :userId")
    fun findAllProductPostIdsByUserId(@Param("userId") userId: String): List<String>
}
