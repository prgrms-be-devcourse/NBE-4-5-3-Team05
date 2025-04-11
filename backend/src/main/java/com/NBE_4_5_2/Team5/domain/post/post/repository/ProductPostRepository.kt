package com.NBE_4_5_2.Team5.domain.post.post.repository

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    @Query(
        """
    SELECT DISTINCT p FROM ProductPost p
    WHERE p.productPrice >= :minPrice AND p.productPrice <= :maxPrice
""", countQuery = """
    SELECT count( p) 
    FROM
        ProductPost p 
    WHERE
        p.productPrice >= :minPrice 
        AND p.productPrice <= :maxPrice
"""
    )
    fun findAllWithPrice(
        @Param("minPrice") minPrice: Int,
        @Param("maxPrice") maxPrice: Int,
        pageable: Pageable
    ): Page<ProductPost>

//    """
//    SELECT p FROM ProductPost p
//    JOIN p.productCategories pc
//    WHERE p.productPrice >= :minPrice AND p.productPrice <= :maxPrice
//    GROUP BY p
//    HAVING COUNT(DISTINCT CASE WHEN pc.category.id IN :categoryIds THEN pc.category.id END) = :categoryCount
//"""

    @Query(
        """
            SELECT p 
FROM ProductPost p 
WHERE  p.productPrice BETWEEN :minPrice AND :maxPrice 
 AND p.id IN (
    SELECT pp.id 
    FROM ProductPost pp 
    JOIN pp.productCategories pc 
    WHERE pp.productPrice BETWEEN :minPrice AND :maxPrice 
    AND pc.category.id IN :categoryIds
    GROUP BY pp.id
    HAVING COUNT(DISTINCT pc.category.id) = :categoryCount
)
ORDER BY p.createdDate DESC
        """
        ,
        countQuery = """
    SELECT count( p) 
    FROM
        ProductPost p 
    WHERE
        p.productPrice >= :minPrice 
        AND p.productPrice <= :maxPrice
""")
    fun findAllWithFilters(
        @Param("minPrice") minPrice: Int,
        @Param("maxPrice") maxPrice: Int,
        @Param("categoryIds") categoryIds: List<Long>,
        @Param("categoryCount") categoryCount: Long,
        pageable: Pageable
    ): Page<ProductPost>

    @Query(
        """
    SELECT DISTINCT p FROM ProductPost p
    WHERE p.title LIKE :keyword
      AND p.productPrice >= :minPrice
      AND p.productPrice <= :maxPrice
""", countQuery = """
    SELECT count( p) 
    FROM
        ProductPost p 
    WHERE p.title LIKE :keyword
        AND p.productPrice >= :minPrice 
        AND p.productPrice <= :maxPrice
"""
    )
    fun findByKeywordWithPrice(
        @Param("keyword") keyword: String,
        @Param("minPrice") minPrice: Int,
        @Param("maxPrice") maxPrice: Int,
        pageable: Pageable
    ): Page<ProductPost>

    @Query("""
    SELECT p FROM ProductPost p
    JOIN p.productCategories pc
    WHERE p.title LIKE :keyword
    AND p.productPrice >= :minPrice AND p.productPrice <= :maxPrice
    GROUP BY p
    HAVING COUNT(DISTINCT CASE WHEN pc.category.id IN :categoryIds THEN pc.category.id END) = :categoryCount
""")
    fun findByKeywordWithFilters(
        @Param("keyword") keyword: String,
        @Param("minPrice") minPrice: Int,
        @Param("maxPrice") maxPrice: Int,
        @Param("categoryIds") categoryIds: List<Long>,
        @Param("categoryCount") categoryCount: Long,
        pageable: Pageable
    ): Page<ProductPost>
}
