package com.NBE_4_5_2.Team5.domain.post.post.repository

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.lang.NonNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductPostRepository : JpaRepository<ProductPost, String> {
    @EntityGraph(attributePaths = ["writer", "productCategories.category"])
    @Query("select p from ProductPost p where p.id = :id")
    fun findByIdWithWriter(id: String): Optional<ProductPost>

    @EntityGraph(attributePaths = ["productCategories.category"])
    fun findByTitleLike(title: String, pageable: Pageable): Page<ProductPost>

    @EntityGraph(attributePaths = ["productCategories.category"])
    @Query("select p from ProductPost p")
    fun findAllWithCategories(pageable: Pageable): Page<ProductPost>

    @EntityGraph(attributePaths = ["productCategories.category"])
    fun findByWriter(writer: User, pageable: Pageable): Page<ProductPost>

    // 페이징 없이 전체 조회
    @EntityGraph(attributePaths = ["writer", "productCategories.category"])
    fun findByWriter(writer: User): List<ProductPost>

    //구매된(판매 완료) 상품들 조회
    @EntityGraph(attributePaths = ["productCategories.category"])
    fun findAllByStatus(status: ProductStatus): List<ProductPost>

    @EntityGraph(attributePaths = ["productCategories.category"])
    fun findByBuyer(buyer: User, pageable: Pageable): Page<ProductPost>

    @EntityGraph(attributePaths = ["writer", "productCategories.category"])
    fun findByIdIn(postIds: List<String>, pageable: Pageable): Page<ProductPost>

    @EntityGraph(attributePaths = ["writer", "productCategories.category"])
    fun findByIdIn(postIds: List<String>): List<ProductPost>

    fun findByWriterAndStatus(writer: User, status: ProductStatus, pageable: Pageable): Page<ProductPost>
}