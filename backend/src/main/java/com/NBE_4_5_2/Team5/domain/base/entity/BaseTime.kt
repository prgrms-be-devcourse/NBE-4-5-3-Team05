package com.NBE_4_5_2.Team5.domain.base.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(
    AuditingEntityListener::class
)
abstract class BaseTime {
    @CreatedDate
    @Column(name = "created_at")
    lateinit var createdDate: LocalDateTime

    @LastModifiedDate
    @Column(name = "modified_at")
    lateinit var modifiedDate: LocalDateTime

    fun setCreateDateNow() {
        this.createdDate = LocalDateTime.now()
        this.modifiedDate = createdDate
    }
}
