package com.NBE_4_5_2.Team5.global;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import com.NBE_4_5_2.Team5.global.security.SecurityUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@RequestScope
public class Rq {

	private final HttpServletResponse response;
	private final HttpServletRequest request;
	private final UserService userService;

	public void setLogin(User actor) {

		UserDetails user = new SecurityUser(actor.getId(), actor.getUsername(), "", List.of(), actor.getRole());

		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

	}

	public User getUserIdentity() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new ServiceException("401-2", "로그인이 필요합니다.");
		}

		Object principal = authentication.getPrincipal();

		if (!(principal instanceof SecurityUser)) {
			throw new ServiceException("401-3", "잘못된 인증 정보입니다");
		}

		SecurityUser user = (SecurityUser)principal;

		return User.builder().id(user.getId()).username(user.getUsername()).build();
	}

	public String getHeader(String name) {
		return request.getHeader(name);
	}

	public User getRealActor(User actor) {
		return userService.findById(actor.getId()).get();
	}

	public String getValueFromCookie(String name) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public void addCookie(String name, String value) {
		Cookie accsessTokenCookie = new Cookie(name, value);

		accsessTokenCookie.setDomain("localhost");
		accsessTokenCookie.setPath("/");
		accsessTokenCookie.setHttpOnly(true);
		accsessTokenCookie.setSecure(true);
		accsessTokenCookie.setAttribute("SameSite", "Strict");

		response.addCookie(accsessTokenCookie);
	}

	public void removeCookie(String name) {

		Cookie cookie = new Cookie(name, null);
		cookie.setDomain("localhost");
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "Strict");
		cookie.setMaxAge(0);

		response.addCookie(cookie);
	}
}
