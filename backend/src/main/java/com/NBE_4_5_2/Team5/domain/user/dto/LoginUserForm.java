package com.NBE_4_5_2.Team5.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginUserForm(
        @NotBlank(message = "아이디는 필수 입력값입니다.") String username,
        @NotBlank(message = "비밀번호는 필수 입력값입니다.") String password
) {
}
