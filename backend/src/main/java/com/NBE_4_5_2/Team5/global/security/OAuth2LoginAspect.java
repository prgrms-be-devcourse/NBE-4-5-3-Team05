package com.NBE_4_5_2.Team5.global.security;

import java.time.LocalDateTime;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginAspect {

	private ThreadLocal<Long> startMilli = new ThreadLocal<>();
	private ThreadLocal<Map<String, Cookie>> createdCookies = new ThreadLocal<>();
	private String username;
	private String providerName;
	private String remoteAddress;
	private LocalDateTime loggedInAt;

	private final AntPathRequestMatcher loginRequestMatcher = new AntPathRequestMatcher(
		"/oauth2/authorization/{registrationId}");

	@Around("execution(* com.NBE_4_5_2.Team5.global.security.CustomAuthorizationRequestResolver.*(..))")
	public Object startLoginOAuth2(ProceedingJoinPoint joinPoint) throws Throwable {

		LocalDateTime now = LocalDateTime.now();

		HttpServletRequest req = (HttpServletRequest)joinPoint.getArgs()[0];

		if (!loginRequestMatcher.matches(req)) {
			return joinPoint.proceed();
		}

		remoteAddress = req.getRemoteAddr();
		loggedInAt = now;

		log.info("""
			[{}] : [{}/{}] OAuth2 로그인 시작.""", now, "Anonymous", "Anonymous");
		return joinPoint.proceed();
	}

	@Around("execution(* com.NBE_4_5_2.Team5.global.security.CustomOAuth2UserService.loadUser(..)))")
	public Object setOAuth2LoginMetaData(ProceedingJoinPoint joinPoint) throws Throwable {

		Object proceed = joinPoint.proceed();

		SecurityUser user = (SecurityUser)proceed;
		username = user.getUsername();
		OAuth2UserRequest oAuth2UserRequest = (OAuth2UserRequest)joinPoint.getArgs()[0];
		providerName = oAuth2UserRequest.getClientRegistration().getRegistrationId();

		return proceed;

	}

	@Around("execution(* com.NBE_4_5_2.Team5.global.security.CustomAuthenticationSuccessHandler.*(..))")
	public Object successOAuth2Login(ProceedingJoinPoint joinPoint) throws Throwable {

		LocalDateTime now = LocalDateTime.now();
		Object res = null;

		log.info("""
				[{}] : [{}/{}] OAuth2 로그인 종료.
				username : {},
				provider : {},
				remote ip : {},
				loggedAt : {}""", now, "Anonymous", "Anonymous",
			username,
			providerName,
			remoteAddress,
			loggedInAt);
		res = joinPoint.proceed();
		return res;
	}
}
