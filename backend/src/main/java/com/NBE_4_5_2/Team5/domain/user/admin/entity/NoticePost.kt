package com.NBE_4_5_2.Team5.domain.user.admin.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@Table(name = "notice_post")
class NoticePost(
    @Id
    @Column(updatable = false, nullable = false, name = "id")
    private var _id: String = "npost-" + UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private var _admin: User,
    @Column(name = "title")
    private var _title: String,
    @Column(name = "content")
    private var _content: String
) : BaseTime() {
    val id:String
        get()=_id
    val admin:User
        get()=_admin
    val title:String
        get()=_title
    val content:String
        get()=_content

    constructor(title: String, content: String, admin: User):this(
        _title=title,
        _content=content,
        _admin=admin
    )

    fun update(title: String, content: String): NoticePost {
        this._title = title
        this._content = content
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (o !is NoticePost) return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}

