package com.NBE_4_5_2.Team5.domain.user.admin.dto

import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Getter
class NoticeDto @Builder constructor(
    private var id: String,
    private var title: String,
    private var content: String,
    private var authorId: String
)
