package com.NBE_4_5_2.Team5.global.security;

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final Rq rq;
	private final UserService userService;
	private final UserAuthService userAuthService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		HttpSession session = request.getSession();
		String redirectUrl = (String)session.getAttribute("redirectUrl");

		if (redirectUrl == null) {
			redirectUrl = "http://localhost:3000";
		}

		session.removeAttribute("redirectUrl");

		/**
		 * refreshToken을 user가 회원가입할 때 부여하는 것이 아니라
		 * login 마다 새로 생성하여 redis에 저장하는 방식으로 변경하였습니다.
		 *
		 *  이에 따라 기존에 실제 유저 객체에서 refreshToken을 가져와 Cookie에 저장하는 방식에서
		 *  refreshToken을 새로 생성하여 redis에 저장하고 쿠키에 넣어주는 방식으로 변경하였습니다.
		 * */

		User user = userAuthService.getUserIdentity();

		//      기존 로직
		//      User realActor = rq.getRealActor(user);
		//
		//      String refreshToken = realActor.getRefreshToken();
		//      String accessToken = userService.generateAccessToken(user);

		AuthToken authToken = userService.generateAuthtoken(user); // user 정보로 refreshToken과 accessToken을 생성
		userService.saveRefreshToken(user, authToken.refreshToken()); // refreshToken을 redis에 저장

		String refreshToken = authToken.refreshToken();
		String accessToken = authToken.accessToken();

		rq.addCookie("refreshToken", refreshToken);
		rq.addCookie("accessToken", accessToken);

		response.sendRedirect(redirectUrl);
	}
}
