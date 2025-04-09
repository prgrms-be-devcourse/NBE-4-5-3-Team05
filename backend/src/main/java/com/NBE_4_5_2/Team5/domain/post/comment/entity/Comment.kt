package com.NBE_4_5_2.Team5.domain.post.comment.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate
import java.util.*

@Entity
class Comment() : BaseTime() {

    @Id
    @Column(updatable = false, nullable = false)
    var id: String = "comment-" + UUID.randomUUID()

    @ManyToOne
    lateinit var target: ProductPost

    @ManyToOne
    lateinit var author: User

    lateinit var content: String

    constructor(content: String, target: ProductPost, author: User):this() {
        this.content = content
        this.target = target
        this.author = author
        target.addComment(this)
        author.addWroteComments(this)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other))
            return false

        other as Comment
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun isMine(loggedInUser: User?) {
        if (!author!!.equals(loggedInUser)) {
            throw ForbiddenAccessException("403-1", "작성자가 아닙니다.")
        }
    }

    fun update(content: String) {
        this.content = content
    }
}
