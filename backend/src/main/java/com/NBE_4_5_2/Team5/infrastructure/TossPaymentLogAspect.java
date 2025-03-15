package com.NBE_4_5_2.Team5.infrastructure;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.NBE_4_5_2.Team5.global.security.SecurityUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentLogAspect {

	@Around("execution(* com.NBE_4_5_2.Team5.infrastructure.*.*(..))")
	public Object responseAspect(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		LocalDateTime now = LocalDateTime.now();
		Object res = null;

		long startTime = System.currentTimeMillis();
		SecurityUser securityUser = (SecurityUser)authentication.getPrincipal();

		log.info("[{}] : [{}/{}] {}.{} 시작", now, securityUser.getId(), securityUser.getRole(),
			className,
			methodName);
		try {
			res = joinPoint.proceed();
			return res;
		} finally {
			long endTime = System.currentTimeMillis();
			log.info("""
					[{}] : [{}/{}] {}.{} 종료.
					elapsed:{}ms,
					orderId : {},
					paymentKey : {},
					amount : {}
					""",
				now, securityUser.getId(), securityUser.getRole(), className, methodName,
				endTime - startTime,
				joinPoint.getArgs()[0],
				joinPoint.getArgs()[1],
				joinPoint.getArgs()[2]
			);
		}
	}

}
