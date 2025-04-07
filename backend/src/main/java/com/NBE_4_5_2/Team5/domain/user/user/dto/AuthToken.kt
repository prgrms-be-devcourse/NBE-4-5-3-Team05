package com.NBE_4_5_2.Team5.domain.user.user.dto

@JvmRecord
data class AuthToken(
    val refreshToken: String, val accessToken: String
)