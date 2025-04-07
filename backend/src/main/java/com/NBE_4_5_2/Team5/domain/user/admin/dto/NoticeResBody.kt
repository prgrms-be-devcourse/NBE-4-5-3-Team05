package com.NBE_4_5_2.Team5.domain.user.admin.dto

import com.NBE_4_5_2.Team5.domain.user.admin.entity.NoticePost
import java.time.LocalDateTime

data class NoticeResBody(
	val id: String,
	val title: String,
	val content: String,
	val admin: AdminResBody,
	val createdAt: LocalDateTime
) {
    companion object {
        @JvmStatic
		fun of(saved: NoticePost): NoticeResBody {
            return NoticeResBody(
                saved.id, saved.title, saved.content,
                AdminResBody(saved.admin), saved.createdAt
            )
        }

    }
}
