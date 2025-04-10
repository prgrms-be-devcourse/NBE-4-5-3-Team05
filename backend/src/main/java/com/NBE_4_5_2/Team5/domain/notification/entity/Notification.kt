package com.NBE_4_5_2.Team5.domain.notification.entity

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.util.logging.Logger.global

@Entity
class Notification(
    @Column(name="user_id")
    @JsonProperty(value = "userId")
    private var _userId: String?,
    @Column(name = "global")
    @JsonProperty(value="global")
    private var _isGlobal: Boolean = false,
    @Column(name = "content")
    @JsonProperty(value="content")
    private var _content: String?=null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty(value = "id")
    private var _id: Long? = null
    val id: Long
        get() = _id!!

    val userId: String?
        get()=_userId

    val global: Boolean
        get()=_isGlobal

    val content: String?
        get()=_content


    constructor(global: Boolean, content: String) : this(
        null,
        _isGlobal = global,
        _content = content
    )
    constructor(id:Long, userId:String?, global:Boolean, content:String?):this(
        _userId=userId,
        _isGlobal=global,
        _content= content,
    ){
        _id=id
    }
}
