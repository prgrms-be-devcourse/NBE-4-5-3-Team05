package com.NBE_4_5_2.Team5.domain.post.comment.repository

import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, String> {
    fun findByTarget_Id(id: String, pageable: Pageable): Slice<Comment>
}