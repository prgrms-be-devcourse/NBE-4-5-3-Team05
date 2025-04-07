package com.NBE_4_5_2.Team5.domain.user.user.dto

@JvmRecord // TODO: 코틀린 변환 완료 후 제거
data class AuthToken(
	val refreshToken: String,
	val accessToken: String
) 