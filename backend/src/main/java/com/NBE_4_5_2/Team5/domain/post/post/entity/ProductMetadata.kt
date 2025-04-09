package com.NBE_4_5_2.Team5.domain.post.post.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class ProductMetadata() {
    @Id
    lateinit var name: String

    @Column(name = "metadata_value")
    lateinit var value: String

    constructor(name: String, value: String): this() {
        this.name = name
        this.value = value
    }
}
