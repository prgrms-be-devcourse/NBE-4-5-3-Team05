package com.NBE_4_5_2.Team5.global.security

import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.rq.Rq
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationSuccessHandler(
    private val rq: Rq,
    private val userService: UserService,
    private val userAuthService: UserAuthService
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val session = request.session
        // 안전한 redirectUrl 추출 & 기본값
        val redirectUrl = (session.getAttribute("redirectUrl") as? String)
            ?.takeIf { it.isNotBlank() }
            ?: "http://localhost:3000"
        session.removeAttribute("redirectUrl")

        /**
         * refreshToken을 user가 회원가입할 때 부여하는 것이 아니라
         * login 마다 새로 생성하여 redis에 저장하는 방식으로 변경하였습니다.
         *
         *  이에 따라 기존에 실제 유저 객체에서 refreshToken을 가져와 Cookie에 저장하는 방식에서
         *  refreshToken을 새로 생성하여 redis에 저장하고 쿠키에 넣어주는 방식으로 변경하였습니다.
         * */

        // 새로 발급한 토큰 생성 & 저장
        val user = userAuthService.userIdentity
        val authToken = userService.generateAuthtoken(user)
        userService.saveRefreshToken(user, authToken.refreshToken)

        // 쿠키에 토큰 추가
        rq.addCookie("refreshToken", authToken.refreshToken)
        rq.addCookie("accessToken", authToken.accessToken)

        // 리다이렉트
        response.sendRedirect(redirectUrl)
    }
}
