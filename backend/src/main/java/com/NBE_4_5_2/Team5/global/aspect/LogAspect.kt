package com.NBE_4_5_2.Team5.global.aspect

import com.NBE_4_5_2.Team5.global.security.SecurityUser
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
class LogAspect {

    companion object {
        private val log = LoggerFactory.getLogger(LogAspect::class.java)
        const val SERVICE_POINTCUT =
            "execution(* com.NBE_4_5_2.Team5..*Service.*(..)) || within(@jakarta.persistence.Entity *)"
        const val CONTROLLER_POINTCUT =
            "execution(* com.NBE_4_5_2.Team5..*Controller.*(..)) && " +
                    "!execution(* com.NBE_4_5_2.Team5.domain.user.user.controller.UserController.me(..)) && " +
                    "!execution(* com.NBE_4_5_2.Team5.domain.user.user.controller.UserController.loginUser(..))"
    }

    @Around(SERVICE_POINTCUT)
    @Throws(Throwable::class)
    fun responseAspect(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.name
        val className = joinPoint.target.javaClass.simpleName
        val startTime = System.currentTimeMillis()
        log.debug("[{}] : {}.{} 시작", LocalDateTime.now(), className, methodName)
        return try {
            joinPoint.proceed()
        } finally {
            val endTime = System.currentTimeMillis()
            log.debug(
                "[{}] : {}.{} 종료, elapsed:{}",
                LocalDateTime.now(),
                className,
                methodName,
                endTime - startTime
            )
        }
    }

    @Around(CONTROLLER_POINTCUT)
    @Throws(Throwable::class)
    fun controllerAspect(joinPoint: ProceedingJoinPoint): Any? {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val now = LocalDateTime.now()
        return if (authentication.principal is SecurityUser) {
            loggingLoggedInUser(authentication, joinPoint, now)
        } else {
            loggingAnonymousUser(joinPoint, now)
        }
    }

    @Throws(Throwable::class)
    private fun loggingAnonymousUser(joinPoint: ProceedingJoinPoint, now: LocalDateTime): Any? {
        val methodName = joinPoint.signature.name
        val className = joinPoint.target.javaClass.simpleName
        val startTime = System.currentTimeMillis()
        log.info("[{}] : [{}/{}] {}.{} 시작", now, "Anonymous", "Anonymous", className, methodName)
        return try {
            joinPoint.proceed()
        } finally {
            val endTime = System.currentTimeMillis()
            log.info(
                "[{}] : [{}/{}] {}.{} 종료, elapsed:{}ms",
                now,
                "Anonymous",
                "Anonymous",
                className,
                methodName,
                endTime - startTime
            )
        }
    }

    @Throws(Throwable::class)
    private fun loggingLoggedInUser(
        authentication: Authentication,
        joinPoint: ProceedingJoinPoint,
        now: LocalDateTime
    ): Any? {
        val methodName = joinPoint.signature.name
        val className = joinPoint.target.javaClass.simpleName
        val startTime = System.currentTimeMillis()
        val securityUser = authentication.principal as SecurityUser
        log.info("[{}] : [{}/{}] {}.{} 시작", now, securityUser.id, securityUser.role, className, methodName)
        return try {
            joinPoint.proceed()
        } finally {
            val endTime = System.currentTimeMillis()
            log.info(
                "[{}] : [{}/{}] {}.{} 종료, elapsed:{}ms",
                now,
                securityUser.id,
                securityUser.role,
                className,
                methodName,
                endTime - startTime
            )
        }
    }
}
