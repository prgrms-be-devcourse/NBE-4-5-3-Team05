package com.NBE_4_5_2.Team5.infrastructure

import com.NBE_4_5_2.Team5.global.security.SecurityUser
import lombok.extern.slf4j.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.hibernate.query.sqm.tree.SqmNode.log
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
class TossPaymentLogAspect {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Around("execution(* com.NBE_4_5_2.Team5.infrastructure.*.*(..))")
    @Throws(Throwable::class)
    fun responseAspect(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.name
        val className = joinPoint.target.javaClass.simpleName

        val authentication = SecurityContextHolder.getContext().authentication
        val now = LocalDateTime.now()
        var res: Any? = null

        val startTime = System.currentTimeMillis()
        val securityUser = authentication.principal as SecurityUser

        log.info("[{}] : [{}/{}] {}.{} 시작", now, securityUser.id, securityUser.role,
            className,
            methodName
        )

        try {
            res = joinPoint.proceed()
            return res
        } finally {
            val endTime = System.currentTimeMillis()
            log.info(
                """
					[{}] : [{}/{}] {}.{} 종료.
					elapsed:{}ms,
					orderId : {},
					paymentKey : {},
					amount : {}
					
					""".trimIndent(),
                now, securityUser.id, securityUser.role, className, methodName,
                endTime - startTime,
                joinPoint.args[0],
                joinPoint.args[1],
                joinPoint.args[2]
            )
        }
    }
}
