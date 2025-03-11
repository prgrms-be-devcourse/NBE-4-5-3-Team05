package com.NBE_4_5_2.Team5.domain.user.logging;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.NBE_4_5_2.Team5.domain.user.controller.UserController;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginLogAspect {

	private final HttpServletRequest request;

	@Around("""
		execution(* com.NBE_4_5_2.Team5.domain.user.controller.UserController.loginUser(..))""")
	public Object userLoginAspect(ProceedingJoinPoint joinPoint) throws Throwable {
		LocalDateTime now = LocalDateTime.now();

		UserController.LoginUserForm userForm = (UserController.LoginUserForm)joinPoint.getArgs()[0];
		String ip = request.getRemoteAddr();
		String accessToken = "";
		String refreshToken = "";
		log.info("[{}] : [{}/{}] 로그인 시작", now, "Anonymous", "Anonymous");

		try {
			Object result = joinPoint.proceed();
			RsData<UserController.LoginUserDto> loginUserDto = (RsData<UserController.LoginUserDto>)result;
			accessToken = loginUserDto.getData().accessToken();
			refreshToken = loginUserDto.getData().refreshToken();
			return result;
		} finally {
			log.info("""
					[{}] : [{}/{}] 로그인 종료
					username: {},
					address : {},
					accessToken : {},
					refreshToken : {}
					""", now, "Anonymous", "Anonymous",
				userForm.username(),
				ip,
				accessToken,
				refreshToken
			);
		}

	}
}

