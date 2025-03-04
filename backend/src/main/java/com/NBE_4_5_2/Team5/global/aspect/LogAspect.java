package com.NBE_4_5_2.Team5.global.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogAspect {

	@Around("""
		execution(* com.NBE_4_5_2.Team5..*Service.*(..)) ||
		within(@jakarta.persistence.Entity *)
		""")
	public Object responseAspect(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getName();
		long startTime = System.currentTimeMillis();
		log.info("[{}] : {}.{} 시작", LocalDateTime.now(), className, methodName);

		try {
			return joinPoint.proceed();
		} finally {

			long endTime = System.currentTimeMillis();
			log.info("[{}] : {}.{} 종료, elapsed:{}", LocalDateTime.now(), className, methodName, endTime - startTime);
		}
	}
}
