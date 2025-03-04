package com.NBE_4_5_2.Team5.global.security;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Rq rq;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String url = request.getRequestURI();

        if (List.of("/api/users/login", "/api/users/signup", "/api/users/refresh").contains(url)) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthToken tokens = getAuthTokenFromRequest();

        if (tokens == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = tokens.refreshToken();
        String accessToken = tokens.accessToken();

        User actor = refreshAccessToken(refreshToken, accessToken);

        if (actor == null) {
            filterChain.doFilter(request, response);
            return;
        }

        rq.setLogin(actor);
        filterChain.doFilter(request, response);
    }

    private boolean isAuthorizationHeader() {
        String authorizationHeader = rq.getHeader("Authorization");

        if (authorizationHeader == null) {
            return false;
        }

        return authorizationHeader.startsWith("Bearer ");
    }


    record AuthToken(String refreshToken, String accessToken) {
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

            return new AuthToken(refreshToken, accessToken);
        }

        String refreshToken = rq.getValueFromCookie("refreshToken");
        String accessToken = rq.getValueFromCookie("accessToken");

        if (refreshToken == null || accessToken == null) {
            return null;
        }

        return new AuthToken(refreshToken, accessToken);

    }

    private User refreshAccessToken(String refreshToken, String accessToken) {

        Optional<User> opAccessUser = userService.getUserByAccessToken(accessToken);

        if (opAccessUser.isPresent()) {
            return opAccessUser.get();
        }

        Optional<User> opRefreshUser = userService.findByRefreshToken(refreshToken);

        if (opRefreshUser.isEmpty()) {
            return null;
        }

        String newAccessToken = userService.generateAccessToken(opRefreshUser.get());
        rq.addCookie("refreshToken", refreshToken);
        rq.addCookie("accessToken", newAccessToken);

        return opRefreshUser.get();
    }
}
