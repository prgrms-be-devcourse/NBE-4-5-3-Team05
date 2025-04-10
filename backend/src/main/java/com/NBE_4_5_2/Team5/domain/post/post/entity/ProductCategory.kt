package com.NBE_4_5_2.Team5.domain.post.post.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseLongIdEntity
import com.NBE_4_5_2.Team5.domain.post.category.entity.Category
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@Entity
@Table(name = "ProductCategory", indexes = [
    Index(name = "idx_productcategory", columnList = "product_post_id, category_id")
])
class ProductCategory(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_post_id", nullable = false)
    var productPost: ProductPost,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category

) : BaseLongIdEntity() {

    companion object {
        
        fun of(productPost: ProductPost, category: Category): ProductCategory {
            return ProductCategory(productPost, category)
        }
    }
}
