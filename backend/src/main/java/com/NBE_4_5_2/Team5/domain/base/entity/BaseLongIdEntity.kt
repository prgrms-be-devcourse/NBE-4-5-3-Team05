package com.NBE_4_5_2.Team5.domain.base.entity

import jakarta.persistence.*
import org.hibernate.Hibernate

@MappedSuperclass
abstract class BaseLongIdEntity {
    @Id // PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Column(name = "id")
    private var _id: Long? = null // TODO: 추후에 전환 과정에서 해결 (private->public)

    var id: Long
    get() = _id ?: 0
    set(value) {
        _id = value
    }

    override fun equals(other: Any?): Boolean {

        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as BaseLongIdEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }
}