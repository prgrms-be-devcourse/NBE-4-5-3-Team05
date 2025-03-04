package com.NBE_4_5_2.Team5.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshUserForm(
        @NotBlank(message = "refreshToken을 입력해주세요.")
        String refreshToken
) {
}
