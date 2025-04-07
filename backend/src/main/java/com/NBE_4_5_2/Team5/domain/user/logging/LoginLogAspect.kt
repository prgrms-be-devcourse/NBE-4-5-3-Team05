package com.NBE_4_5_2.Team5.domain.user.logging

import com.NBE_4_5_2.Team5.domain.user.user.controller.UserController.LoginUserDto
import com.NBE_4_5_2.Team5.domain.user.user.controller.UserController.LoginUserForm
import com.NBE_4_5_2.Team5.global.dto.RsData
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.hibernate.query.sqm.tree.SqmNode.log
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
@RequiredArgsConstructor
class LoginLogAspect(
    private val request: HttpServletRequest
) {
    val log = LoggerFactory.getLogger(LoginLogAspect::class.java)


    @Around(
		"execution(* com.NBE_4_5_2.Team5.domain.user.user.controller.UserController.loginUser(..))"
    )
    @Throws(Throwable::class)
    fun userLoginAspect(joinPoint: ProceedingJoinPoint): Any {
        val now = LocalDateTime.now()

        val userForm = joinPoint.args[0] as LoginUserForm
        val ip = request!!.remoteAddr
        var accessToken = ""
        var refreshToken = ""
        log.info("[{}] : [{}/{}] 로그인 시작", now, "Anonymous", "Anonymous")

        try {
            val result = joinPoint.proceed()
            val loginUserDto = result as RsData<LoginUserDto>
            accessToken = loginUserDto.data.accessToken
            refreshToken = loginUserDto.data.refreshToken
            return result
        } finally {
            log.info(
                """
					[{}] : [{}/{}] 로그인 종료
					username: {},
					address : {},
					accessToken : {},
					refreshToken : {}
					
					""".trimIndent(), now, "Anonymous", "Anonymous",
                userForm.username,
                ip,
                accessToken,
                refreshToken
            )
        }
    }
}

