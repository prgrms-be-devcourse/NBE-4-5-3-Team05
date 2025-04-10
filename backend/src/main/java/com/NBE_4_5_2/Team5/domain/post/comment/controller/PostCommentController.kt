package com.NBE_4_5_2.Team5.domain.post.comment.controller

import com.NBE_4_5_2.Team5.domain.post.comment.dto.CommentDto
import com.NBE_4_5_2.Team5.domain.post.comment.service.CommentService
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import com.NBE_4_5_2.Team5.global.dto.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Comment API", description = "댓글 API")
class PostCommentController(
    private val commentService: CommentService

) {
    @JvmRecord
    data class WriteCommentResBody(val id: String, val content: String, val author: UserDto)

    @JvmRecord
    data class WriteCommentReqBody(
        @Parameter(description = "댓글 내용") @Parameter(
            description = "댓글 내용"
        ) val content: String
    )

    @Operation(summary = "댓글 작성", description = "상품에 댓글을 작성합니다.")
    @PostMapping("/{post-id}/comments")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    fun writeComment(
        @Parameter(description = "작성할 상품 게시글 id") @PathVariable(name = "post-id") postId: String,
        @RequestBody body: WriteCommentReqBody
    ): RsData<CommentDto> {
        val commentDto = commentService.writeComment(postId, body.content)

        return RsData(
            "200-1", "댓글 작성 성공.",
            commentDto
        )
    }

    @JvmRecord
    data class UpdateCommentResBody(val content: String, val author: UserDto)

    @JvmRecord
    data class UpdateCommentReqBody(val content: String)

    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @PutMapping("/{post-id}/comments/{comment-id}")
    fun updateComment(
        @PathVariable(name = "comment-id") commentId: String,
        @RequestBody body: UpdateCommentReqBody
    ): RsData<UpdateCommentResBody> {
        val commentDto = commentService.updateComment(commentId, body.content)

        return RsData(
            "200-1", "comment 수정 성공.",
            UpdateCommentResBody(commentDto.content, commentDto.author)
        )
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @DeleteMapping("/{post-id}/comments/{comment-id}")
    fun deleteComment(@PathVariable(name = "comment-id") commentId: String): RsData<Void> {
        commentService.deleteComment(commentId)

        return RsData("204-1", "comment 삭제 성공.")
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    @GetMapping("/{id}/comments")
    fun getComments(@PathVariable(name = "id") postId: String, pageable: Pageable): RsData<Slice<CommentDto>> {
        val comments = commentService.getComments(postId, pageable)

        return RsData("200-1", "comment 조회 성공", comments)
    }
}
