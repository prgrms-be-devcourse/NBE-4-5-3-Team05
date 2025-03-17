package com.NBE_4_5_2.Team5.global;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

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

	public String getHeader(String name) {
		return request.getHeader(name);
	}

	public Optional<String> getRefreshToken() {
		String refreshToken = getValueFromCookie("refreshToken");

		if (refreshToken == null) {
			return Optional.empty();
		}

		return Optional.of(refreshToken);
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

		accsessTokenCookie.setPath("/");
		accsessTokenCookie.setHttpOnly(true);
		accsessTokenCookie.setSecure(true);
		accsessTokenCookie.setAttribute("SameSite", "None");

		response.addCookie(accsessTokenCookie);
	}

	public void removeCookie(String name) {

		Cookie cookie = new Cookie(name, null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "None");
		cookie.setMaxAge(0);

		response.addCookie(cookie);
	}
}
