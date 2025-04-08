package com.NBE_4_5_2.Team5.global.security

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
class OAuth2LoginAspect {

    private val log = LoggerFactory.getLogger(javaClass)
    private val loginMatcher = AntPathRequestMatcher("/oauth2/authorization/{registrationId}")

    // ThreadLocal 로 모두 감싸기
    private val usernameLocal    = ThreadLocal<String>()
    private val providerLocal    = ThreadLocal<String>()
    private val remoteAddrLocal  = ThreadLocal<String>()
    private val loginAtLocal     = ThreadLocal<LocalDateTime>()

    @Around("execution(* com.NBE_4_5_2.Team5.global.security.CustomAuthenticationSuccessHandler.*(..))")
    fun startLoginOAuth2(joinPoint: ProceedingJoinPoint): Any? {
        val req = joinPoint.args.getOrNull(0) as? HttpServletRequest
            ?: return joinPoint.proceed()

        if (!loginMatcher.matches(req)) {
            return joinPoint.proceed()
        }

        val now = LocalDateTime.now()
        remoteAddrLocal.set(req.remoteAddr)
        loginAtLocal.set(now)
        log.info("[{}] : OAuth2 로그인 시작 (ip={})", now, req.remoteAddr)

        return joinPoint.proceed()
    }

    @Around("execution(* com.NBE_4_5_2.Team5.global.security.CustomOAuth2UserService.loadUser(..))")
    fun setOAuth2LoginMetaData(joinPoint: ProceedingJoinPoint): Any? {
        val result = joinPoint.proceed()
        // OAuth2UserRequest 꺼내서 provider, SecurityUser 꺼내서 username
        (joinPoint.args.getOrNull(0) as? OAuth2UserRequest)
            ?.clientRegistration
            ?.registrationId
            ?.let { providerLocal.set(it) }

        (result as? SecurityUser)
            ?.username
            ?.let { usernameLocal.set(it) }

        return result
    }

    @Around("execution(* com.NBE_4_5_2.Team5.global.security.CustomAuthenticationSuccessHandler.onAuthenticationSuccess(..))")
    fun successOAuth2Login(joinPoint: ProceedingJoinPoint): Any? {
        val result = joinPoint.proceed()
        val now = LocalDateTime.now()
        val user    = usernameLocal.get()   ?: "Anonymous"
        val prov    = providerLocal.get()   ?: "Unknown"
        val ip      = remoteAddrLocal.get() ?: "Unknown"
        val at      = loginAtLocal.get()    ?: now

        log.info(
            "[{}] : OAuth2 로그인 종료. user={}, provider={}, ip={}, at={}",
            now, user, prov, ip, at
        )

        // ThreadLocal 클리어
        usernameLocal.remove()
        providerLocal.remove()
        remoteAddrLocal.remove()
        loginAtLocal.remove()

        return result
    }
}
