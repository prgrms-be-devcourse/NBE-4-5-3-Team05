package com.NBE_4_5_2.Team5.global.rq

import java.util.Optional

interface Rq {
    val refreshToken: Optional<String>

    fun getValueFromCookie(accessToken: String): String?

    fun addCookie(accessToken: String, s: String)

    fun getHeader(authorization: String): String?

    fun removeCookie(accessToken: String)
}
