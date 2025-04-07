package com.NBE_4_5_2.Team5.domain.post.category.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseLongIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Category : BaseLongIdEntity() {
    @Column(nullable = false, unique = true)
    lateinit var name: String
}