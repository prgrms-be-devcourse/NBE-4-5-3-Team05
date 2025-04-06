package com.NBE_4_5_2.Team5.global.security

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.Rq
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class CustomAuthenticationFilter(
    private val rq: Rq,
    private val userService: UserService,
    private val userAuthService: UserAuthService
) : OncePerRequestFilter() {

    companion object {
        private val EXCLUDED_URLS = setOf(
            "/api/users/login",
            "/api/users/signup",
            "/api/users/refresh",
            "/api/users/email/code/verify",
            "/api/users/email/code",
            "/error",
            "/actuator/**",
            "/swagger-ui/**"
        )
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val url = request.requestURI
        if (EXCLUDED_URLS.contains(url)) {
            filterChain.doFilter(request, response)
            return
        }

        val tokens = authTokenFromRequest

        if (tokens == null) {
            filterChain.doFilter(request, response)
            return
        }

        val (refreshToken, accessToken) = tokens
        val actor = getUserByAccessToken(accessToken, refreshToken)

        if (actor == null) {
            filterChain.doFilter(request, response)
            return
        }

        userAuthService.setLogin(actor)
        filterChain.doFilter(request, response)
    }

    private val isAuthorizationHeader: Boolean
        get()  = rq.getHeader("Authorization")
                ?.startsWith("Bearer ")
                ?: false

    private val authTokenFromRequest: AuthToken?
        get() {
            if (isAuthorizationHeader) {
                val authorizationHeader = rq.getHeader("Authorization")
                val authToken = authorizationHeader?.removePrefix("Bearer ")
                val tokenBits = authToken?.split(" ", limit = 2)

                if (tokenBits?.size != 2) {
                    return null
                }

                val refreshToken = tokenBits[0]
                val accessToken = tokenBits[1]

                if (refreshToken.isBlank() || accessToken.isBlank()) {
                    return null
                }

                return AuthToken(refreshToken, accessToken)
            }

            val refreshToken = rq.getValueFromCookie("refreshToken")
            val accessToken = rq.getValueFromCookie("accessToken")

            if (refreshToken == null || accessToken == null) {
                return null
            }

            return AuthToken(refreshToken, accessToken)
        }

    /**
     * accessToken 재발급 로직
     *
     *
     * accessToken 재발급 시 refreshToken 또한 재발급하며 기존 refreshToken을 Redis에서 제거한다.
     * - 현재 refreshToken은 로그아웃 시에만 삭제되므로,
     * 사용자가 로그아웃하지 않는다면 탈취된 refreshToken으로 지속적인 재발급이 가능해지는 보안 문제가 발생한다.
     *
     *
     * 1. Redis에 refreshToken을 저장하고 만료 시간을 설정하여 1차 방지
     * 2. accessToken 재발급 시 기존 refreshToken을 저장소에서 제거하는 것으로 재발급을 1회로 제한하여 2차 방지
     *
     *
     * ⚠️ 실제 사용자도 재발급이 1회만 가능해지기 때문에 사용자 경험이 저하될 수 있다.
     * 이는 accessToken의 유효기간을 1시간으로 설정하여 보완한다.
     */
    private fun getUserByAccessToken(accessToken: String, refreshToken: String): User? {
        // accessToken이 유효하다면 해당 user 정보를 반환

        val opAccessUser = userService.getUserByAccessToken(accessToken)

        if (opAccessUser.isPresent) {
            return opAccessUser.get()
        }

        val opRefreshUser = userService.getUserByRefreshToken(refreshToken)

        if (opRefreshUser.isEmpty) {
            return null
        }

        val newAuthToken = userService.generateAuthtoken(opRefreshUser.get())
        rq.addCookie("accessToken", newAuthToken.accessToken)

        return opRefreshUser.get()
    }
}
