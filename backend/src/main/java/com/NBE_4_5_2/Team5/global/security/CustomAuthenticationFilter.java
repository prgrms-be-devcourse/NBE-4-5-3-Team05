package com.NBE_4_5_2.Team5.global.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.NBE_4_5_2.Team5.domain.user.user.dto.AuthToken;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

	private final Rq rq;
	private final UserService userService;
	private final UserAuthService userAuthService;

    private static final Set<String> EXCLUDED_URLS = Set.of(
            "/api/users/login",
            "/api/users/signup",
            "/api/users/refresh",
            "/api/users/email/code/verify",
            "/api/users/email/code",
            "/error",
			"/actuator/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String url = request.getRequestURI();
        if (EXCLUDED_URLS.contains(url)) {
            filterChain.doFilter(request, response);
            return;
        }

		AuthToken tokens = getAuthTokenFromRequest();

		if (tokens == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String accessToken = tokens.accessToken();
		String refreshToken = tokens.refreshToken();
		User actor = getUserByAccessToken(accessToken, refreshToken);

		if (actor == null) {
			filterChain.doFilter(request, response);
			return;
		}

		userAuthService.setLogin(actor);
		filterChain.doFilter(request, response);
	}

	private boolean isAuthorizationHeader() {
		String authorizationHeader = rq.getHeader("Authorization");

		if (authorizationHeader == null) {
			return false;
		}

		return authorizationHeader.startsWith("Bearer ");
	}

	private AuthToken getAuthTokenFromRequest() {

		if (isAuthorizationHeader()) {

			String authorizationHeader = rq.getHeader("Authorization");
			String authToken = authorizationHeader.substring("Bearer ".length());

			String[] tokenBits = authToken.split(" ", 2);

			if (tokenBits.length < 2) {
				return null;
			}

			String refreshToken = tokenBits[0];
			String accessToken = tokenBits[1];

			if (refreshToken.isBlank() || accessToken.isBlank()) {
				return null;
			}

			return new AuthToken(refreshToken, accessToken);
		}

		String refreshToken = rq.getValueFromCookie("refreshToken");
		String accessToken = rq.getValueFromCookie("accessToken");

		if (refreshToken == null || accessToken == null) {
			return null;
		}

		return new AuthToken(refreshToken, accessToken);

	}

    /**
     * accessToken 재발급 로직
     * <p>
     * accessToken 재발급 시 refreshToken 또한 재발급하며 기존 refreshToken을 Redis에서 제거한다.
     * - 현재 refreshToken은 로그아웃 시에만 삭제되므로,
     * 사용자가 로그아웃하지 않는다면 탈취된 refreshToken으로 지속적인 재발급이 가능해지는 보안 문제가 발생한다.
     * <p>
     * 1. Redis에 refreshToken을 저장하고 만료 시간을 설정하여 1차 방지
     * 2. accessToken 재발급 시 기존 refreshToken을 저장소에서 제거하는 것으로 재발급을 1회로 제한하여 2차 방지
     * <p>
     * ⚠️ 실제 사용자도 재발급이 1회만 가능해지기 때문에 사용자 경험이 저하될 수 있다.
     * 이는 accessToken의 유효기간을 1시간으로 설정하여 보완한다.
     */
    private User getUserByAccessToken(String accessToken, String refreshToken) {

		// accessToken이 유효하다면 해당 user 정보를 반환
		Optional<User> opAccessUser = userService.getUserByAccessToken(accessToken);

		if (opAccessUser.isPresent()) {
			return opAccessUser.get();
		}

		Optional<User> opRefreshUser = userService.getUserByRefreshToken(refreshToken);

		if (opRefreshUser.isEmpty()) {
			return null;
		}

		AuthToken newAuthToken = userService.generateAuthtoken(opRefreshUser.get());
		rq.addCookie("accessToken", newAuthToken.accessToken());

		return opRefreshUser.get();
	}
}
