package com.NBE_4_5_2.Team5.domain.post.comment.dto

import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment
import com.NBE_4_5_2.Team5.domain.user.user.dto.UserDto
import lombok.Getter
import lombok.NoArgsConstructor

class CommentDto private constructor(
    var id: String,
    var author: UserDto,
    var content: String,
    var postId: String
) {
    companion object {
        fun of(comment: Comment): CommentDto {
            return CommentDto(
                comment.id, UserDto(comment.author), comment.content,
                comment.target.id
            )
        }
    }
}
