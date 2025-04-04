package com.NBE_4_5_2.Team5.domain.user.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UserUpdateRequest(
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    var nickname: String? = null,

    @field:Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
    var address: String? = null,

    @field:Size(max = 255, message = "프로필 URL은 최대 255자까지 입력 가능합니다.")
    var profileUrl: String? = null,

    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    var email: String? = null
)
