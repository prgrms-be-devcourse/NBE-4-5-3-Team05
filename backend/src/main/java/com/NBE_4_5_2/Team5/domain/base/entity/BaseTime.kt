package com.NBE_4_5_2.Team5.domain.base.entity

import jakarta.persistence.MappedSuperclass
import jakarta.persistence.EntityListeners
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseTime {
    @CreatedDate
    lateinit var createdAt: LocalDateTime
        protected set

    @LastModifiedDate
    lateinit var modifiedAt: LocalDateTime
        protected set
}
