package com.NBE_4_5_2.Team5.global.aspect

import com.NBE_4_5_2.Team5.global.response.RsData
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class ResponseAspect(
    private val response: HttpServletResponse
) {

    @Around("execution(* com.NBE_4_5_2.Team5..*Controller.*(..))")
    @Throws(Throwable::class)
    fun responseAspect(joinPoint: ProceedingJoinPoint): Any? {
        val result = joinPoint.proceed()

        // 응답코드를 설정해준다.
        if (result is RsData<*>) {
            val statusCode = result.statusCode
            response.status = statusCode
        }
        return result
    }
}
