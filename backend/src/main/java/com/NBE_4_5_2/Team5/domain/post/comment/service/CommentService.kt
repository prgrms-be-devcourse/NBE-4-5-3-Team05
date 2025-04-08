package com.NBE_4_5_2.Team5.domain.post.comment.service

import com.NBE_4_5_2.Team5.domain.post.comment.dto.CommentDto
import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment
import com.NBE_4_5_2.Team5.domain.post.comment.repository.CommentRepository
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.exception.post.product.ProductPostNotFoundException
import jakarta.persistence.EntityNotFoundException
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@RequiredArgsConstructor
class CommentService(
    private val userService: UserService,
    private val productPostRepository: ProductPostRepository,
    private val commentRepository: CommentRepository,
) {

    @Transactional
    fun writeComment(postId: String, content: String): CommentDto {
        return productPostRepository.findById(postId)
            .orElseThrow {
                ProductPostNotFoundException(
                    "400-1",
                    "id가 $postId 인 product post는 없습니다."
                )
            }
            .let {
                Comment(
                    content,
                    it,
                    loggedInUser
                )
            }.let {
                commentRepository.save(it)

            }.let {
                CommentDto.of(it)
            }
    }

    private val loggedInUser: User
        get() = userService.userIdentity

    @Transactional
    fun updateComment(commentId: String, content: String): CommentDto {

        return commentRepository.findById(commentId)
            .orElseThrow {
                EntityNotFoundException(
                    "id가 $commentId 인 comment를 찾을 수 없습니다."
                )
            }
            .apply {
                isMine(loggedInUser)
                update(content)
            }.let {
                CommentDto.of(it)
            }

    }

    fun deleteComment(commentId: String) {

        commentRepository.findById(commentId)
            .orElseThrow {
                EntityNotFoundException(
                    "id가 $commentId 인 comment를 찾을 수 없습니다."
                )
            }
            .apply {
                isMine(loggedInUser)
            }.run {
                commentRepository.delete(this)
            }
    }

    fun getComments(postId: String, pageable: Pageable): Slice<CommentDto> {
        return commentRepository.findByTarget_Id(postId, pageable)
            .map { comment: Comment ->
                CommentDto.of(
                    comment
                )
            }
    }
}
