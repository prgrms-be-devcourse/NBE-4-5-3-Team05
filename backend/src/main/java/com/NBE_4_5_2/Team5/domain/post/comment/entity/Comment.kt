package com.NBE_4_5_2.Team5.domain.post.comment.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException
import com.querydsl.core.types.Projections.constructor
import jakarta.persistence.*
import lombok.EqualsAndHashCode
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
class Comment(
    @Id
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private var _id: String = "comment-" + UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "target_id")
    private var _target: ProductPost,
    @ManyToOne
    private var _author: User,
    private var _content: String,
) : BaseTime() {
    val id:String
        get()=_id

    val target:ProductPost
        get()=_target

    val author:User
        get()=_author

    val content:String
        get()=_content


    constructor(content: String, target: ProductPost, author: User):this(
        _content=content,
        _target=target,
        _author=author
    ) {
        target.addComment(this)
        author.addWroteComments(this)
    }

    override fun equals(o: Any?): Boolean {
        if (o !is Comment) return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    fun isMine(loggedInUser: User?) {
        if (!author!!.equals(loggedInUser)) {
            throw ForbiddenAccessException("403-1", "작성자가 아닙니다.")
        }
    }

    fun update(content: String) {
        this._content = content
    }
}
