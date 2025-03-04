package com.NBE_4_5_2.Team5.global.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.NBE_4_5_2.Team5.global.response.RsData;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ResponseAspect {
	private final HttpServletResponse response;

	@Around("execution(* com.NBE_4_5_2.Team5..*Controller.*(..))")
	public Object responseAspect(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = joinPoint.proceed();

		// 응답코드를 설정해준다
		if (result instanceof RsData<?> rsData) {
			int statusCode = rsData.getStatusCode();
			response.setStatus(statusCode);
		}

		return result;
	}
}
