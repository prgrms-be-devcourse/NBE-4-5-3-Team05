package com.NBE_4_5_2.Team5.domain.user.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@JvmRecord // TODO: 코틀린 변환 완료 후 제거
data class SignUpUserForm(
    @field:Pattern(
        regexp = "^[a-zA-Z0-9]+$",
        message = "아이디는 영문과 숫자만 사용할 수 있습니다."
    )
    @field:Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    val username: String?,

    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    @field:Size(min = 8, max = 50, message = "비밀번호는 8~50자 사이여야 합니다.")
    val password: String?,

    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String?,

    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    val nickname: String?,

    @field:Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
    val address: String?,

    @field:Size(max = 255, message = "프로필 URL은 최대 255자까지 입력 가능합니다.")
    val profileUrl: String?
)
