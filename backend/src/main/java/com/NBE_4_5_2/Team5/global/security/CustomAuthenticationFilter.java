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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Rq rq;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if(!authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authToken = authorizationHeader.substring("Bearer ".length());

        String[] tokenBits = authToken.split(" ", 2);

        if(tokenBits.length < 2) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = tokenBits[0];
        String accessToken = tokenBits[1];

        Optional<User> opAccessUser = userService.getUserByAccessToken(accessToken);

        if(opAccessUser.isEmpty()) {

            Optional<User> opRefreshUser = userService.findByRefreshToken(refreshToken);

            if(opRefreshUser.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            // accessToken 재발급
            String newAuthToken = userService.getAuthToken(opRefreshUser.get());
            response.addHeader("Authorization", "Bearer " + newAuthToken);

            User actor = opRefreshUser.get();
            rq.setLogin(actor);

            filterChain.doFilter(request, response);
            return;
        }

        User actor = opAccessUser.get();
        rq.setLogin(actor);

        filterChain.doFilter(request, response);
    }
}
