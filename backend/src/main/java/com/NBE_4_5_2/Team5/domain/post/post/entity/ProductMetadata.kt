package com.NBE_4_5_2.Team5.domain.post.post.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class ProductMetadata(
    @Id
    var name: String,

    @Column(name = "metadata_value")
    var value: String
) {

}
