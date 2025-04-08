package com.NBE_4_5_2.Team5.global

import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.util.*

@Component
@RequestScope
@Profile("prod")
class ProductionRq : Rq {
    private val response: HttpServletResponse? = null
    private val request: HttpServletRequest? = null
    private val userService: UserService? = null

    @Value("\${custom.domain}")
    private val domain: String? = null

    override fun getHeader(name: String): String? {
        return request!!.getHeader(name)
    }

    override val refreshToken: Optional<String>
        get() {
            val refreshToken = getValueFromCookie("refreshToken") ?: return Optional.empty()

            return Optional.of(refreshToken)
        }

    override fun getValueFromCookie(name: String): String? {
        val cookies = request!!.cookies ?: return null

        for (cookie in cookies) {
            if (cookie.name == name) {
                return cookie.value
            }
        }
        return null
    }

    override fun addCookie(name: String, value: String) {
        val accsessTokenCookie = Cookie(name, value)

        accsessTokenCookie.domain = domain
        accsessTokenCookie.path = "/"
        accsessTokenCookie.isHttpOnly = true
        accsessTokenCookie.secure = true
        accsessTokenCookie.setAttribute("SameSite", "None")

        response!!.addCookie(accsessTokenCookie)
    }

    override fun removeCookie(name: String) {
        val cookie = Cookie(name, null)
        cookie.domain = domain
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.secure = true
        cookie.setAttribute("SameSite", "None")
        cookie.maxAge = 0

        response!!.addCookie(cookie)
    }
}