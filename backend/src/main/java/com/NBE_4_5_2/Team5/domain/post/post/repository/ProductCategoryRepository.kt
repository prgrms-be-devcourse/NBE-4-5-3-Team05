package com.NBE_4_5_2.Team5.domain.post.post.repository

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductCategoryRepository : JpaRepository<ProductCategory, Long>