package com.NBE_4_5_2.Team5.domain.user.dto;

public record LoginUserDto(
        String accessToken,
        String refreshToken,
        UserDto item
) {
}
