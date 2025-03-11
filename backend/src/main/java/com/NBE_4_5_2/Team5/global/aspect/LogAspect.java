package com.NBE_4_5_2.Team5.global.aspect;

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
public class LogAspect {

	@Around("""
		execution(* com.NBE_4_5_2.Team5..*Service.*(..)) ||
		within(@jakarta.persistence.Entity *)
		""")
	public Object responseAspect(ProceedingJoinPoint joinPoint) throws Throwable {
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		long startTime = System.currentTimeMillis();
		log.debug("[{}] : {}.{} 시작", LocalDateTime.now(), className, methodName);

		try {
			return joinPoint.proceed();
		} finally {

			long endTime = System.currentTimeMillis();
			log.debug("[{}] : {}.{} 종료, elapsed:{}", LocalDateTime.now(), className, methodName, endTime - startTime);
		}
	}

	@Around(value = """
		execution(* com.NBE_4_5_2.Team5..*Controller.*(..)) &&
		!execution(* com.NBE_4_5_2.Team5.domain.user.controller.UserController.me(..)) &&
		!execution(* com.NBE_4_5_2.Team5.domain.user.controller.UserController.loginUser(..))""")
	public Object controllerAspect(ProceedingJoinPoint joinPoint) throws Throwable {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		LocalDateTime now = LocalDateTime.now();
		if (authentication.getPrincipal() instanceof SecurityUser) {
			return loggingLoggedInUser(authentication, joinPoint, now);
		} else {
			return loggingAnonymousUser(joinPoint, now);
		}

	}

	private Object loggingAnonymousUser(ProceedingJoinPoint joinPoint, LocalDateTime now) throws Throwable {
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		long startTime = System.currentTimeMillis();
		log.info("[{}] : [{}/{}] {}.{} 시작", now, "Anonymous", "Anonymous",
			className,
			methodName);
		try {
			return joinPoint.proceed();
		} finally {
			long endTime = System.currentTimeMillis();
			log.info("[{}] : [{}/{}] {}.{} 종료, elapsed:{}ms", now, "Anonymous", "Anonymous",
				className,
				methodName, endTime - startTime);
		}
	}

	private Object loggingLoggedInUser(Authentication authentication, ProceedingJoinPoint joinPoint,
		LocalDateTime now) throws Throwable {
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		long startTime = System.currentTimeMillis();
		SecurityUser securityUser = (SecurityUser)authentication.getPrincipal();
		log.info("[{}] : [{}/{}] {}.{} 시작", now, securityUser.getId(), securityUser.getRole(),
			className,
			methodName);
		try {
			return joinPoint.proceed();
		} finally {
			long endTime = System.currentTimeMillis();
			log.info("[{}] : [{}/{}] {}.{} 종료, elapsed:{}ms", now, securityUser.getId(),
				securityUser.getRole(),
				className,
				methodName, endTime - startTime);
		}
	}

}
