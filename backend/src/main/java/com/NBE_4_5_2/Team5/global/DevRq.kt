package com.NBE_4_5_2.Team5.global

import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.util.Optional

@Component
@RequestScope
@Profile("!prod")
class DevRq(
    private val response: HttpServletResponse,
    private val request: HttpServletRequest,
    private val userService: UserService
) : Rq {

    override fun getHeader(name: String): String? =
        request.getHeader(name)

    override val refreshToken: Optional<String>
        get() {
            val token = getValueFromCookie("refreshToken")
            return if (token == null) Optional.empty() else Optional.of(token)
        }

    override fun getValueFromCookie(name: String): String? {
        val cookies = request.cookies ?: return null
        for (cookie in cookies) {
            if (cookie.name == name) {
                return cookie.value
            }
        }
        return null
    }

    override fun addCookie(name: String, value: String) {
        val cookie = Cookie(name, value).apply {
            path = "/"
            isHttpOnly = true
            secure = true
            setAttribute("SameSite", "Strict")
        }
        response.addCookie(cookie)
    }

    override fun removeCookie(name: String) {
        val cookie = Cookie(name, null).apply {
            path = "/"
            isHttpOnly = true
            secure = true
            setAttribute("SameSite", "Strict")
            maxAge = 0
        }
        response.addCookie(cookie)
    }
}
