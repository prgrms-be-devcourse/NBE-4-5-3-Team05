package com.NBE_4_5_2.Team5.domain.post.post.repository

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductMetadata
import org.springframework.data.jpa.repository.JpaRepository

interface ProductMetadataRepository : JpaRepository<ProductMetadata, String> {
    fun findByName(name: String): List<ProductMetadata>
}