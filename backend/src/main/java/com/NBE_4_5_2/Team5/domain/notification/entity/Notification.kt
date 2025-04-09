package com.NBE_4_5_2.Team5.domain.notification.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.logging.Logger.global

@Entity
class Notification(
    private var _userId: String?,
    private var _isGlobal: Boolean = false,
    private var _content: String?=null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var _id: Long? = null
    val id: Long
        get() = _id!!

    val userId: String
        get()=_userId!!

    val global: Boolean
        get()=_isGlobal

    val content: String?
        get()=_content


    constructor(global: Boolean, content: String) : this(
        null,
        _isGlobal = global,
        _content = content
    )
}
