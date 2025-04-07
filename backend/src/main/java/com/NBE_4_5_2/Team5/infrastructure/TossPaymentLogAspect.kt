package com.NBE_4_5_2.Team5.infrastructure

import com.NBE_4_5_2.Team5.global.security.SecurityUser
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
class TossPaymentLogAspect {

    private val log = LoggerFactory.getLogger(javaClass)

    @Around("execution(* com.NBE_4_5_2.Team5.infrastructure.*.*(..))")
    @Throws(Throwable::class)
    fun logExecution(joinPoint: ProceedingJoinPoint): Any? {
        val className = joinPoint.target.javaClass.simpleName
        val methodName = joinPoint.signature.name
        val now = LocalDateTime.now()
        val startTime = System.currentTimeMillis()

        // SecurityContext에서 사용자 정보 안전하게 꺼내기
        val principal = SecurityContextHolder.getContext().authentication.principal
        val user = principal as? SecurityUser
        val userId = user?.id ?: "Anonymous"
        val role = user?.role ?: "N/A"

        log.info("[$now] : [$userId/$role] $className.$methodName 시작")

        try {
            return joinPoint.proceed()
        } finally {
            val elapsed = System.currentTimeMillis() - startTime
            // 안전하게 인자 꺼내기
            val args = joinPoint.args
            val orderId    = args.getOrNull(0) ?: "unknown"
            val paymentKey = args.getOrNull(1) ?: "unknown"
            val amount     = args.getOrNull(2) ?: "unknown"

            log.info(
                "[$now] : [$userId/$role] $className.$methodName 종료. " +
                        "elapsed=${elapsed}ms, orderId=$orderId, paymentKey=$paymentKey, amount=$amount"
            )
        }
    }
}
