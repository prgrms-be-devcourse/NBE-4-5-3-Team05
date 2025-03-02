package com.NBE_4_5_2.Team5.global;

import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@RequestScope
public class Rq {

    private final HttpServletRequest request;
    private final UserService userService;

    public com.NBE_4_5_2.Team5.domain.user.entity.User getAuthenticatedActor() {
        // TODO: 경로 수정, User 이름 중복
        String authorizationValue = request.getHeader("Authorization");
        String refreshToken = authorizationValue.substring("Bearer ".length());
        Optional<com.NBE_4_5_2.Team5.domain.user.entity.User> opActor = userService.findByRefreshToken(refreshToken);

        if(opActor.isEmpty()) {
            throw new ServiceException("401-1", "잘못된 인증키입니다.");
        }

        return opActor.get();

    }

    public void setLogin(String username) {

        UserDetails user = new User(username, "", List.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }

    public com.NBE_4_5_2.Team5.domain.user.entity.User getActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null) {
            throw new ServiceException("401-2", "로그인이 필요합니다.");
        }

        UserDetails user = (UserDetails) authentication.getPrincipal();

        if(user == null) {
            throw new ServiceException("401-3", "로그인이 필요합니다.");
        }

        String username = user.getUsername();
        return userService.findByUsername(username).get();

    }
}
