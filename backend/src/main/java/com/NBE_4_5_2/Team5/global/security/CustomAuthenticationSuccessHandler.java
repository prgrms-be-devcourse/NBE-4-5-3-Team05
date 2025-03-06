package com.NBE_4_5_2.Team5.global.security;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();
        String redirectUrl = (String) session.getAttribute("redirectUrl");

        if (redirectUrl == null) {
            redirectUrl = "http://localhost:3000";
        }

        session.removeAttribute("redirectUrl");

        User user = rq.getUserIdentity();
        User realActor = rq.getRealActor(user);

        String refreshToken = realActor.getRefreshToken();
        String accessToken = userService.generateAccessToken(user);

        rq.addCookie("refreshToken", refreshToken);
        rq.addCookie("accessToken", accessToken);

        response.sendRedirect(redirectUrl);
    }
}
