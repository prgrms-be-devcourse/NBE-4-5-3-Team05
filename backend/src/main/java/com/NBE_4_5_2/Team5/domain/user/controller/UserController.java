package com.NBE_4_5_2.Team5.domain.user.controller;

import com.NBE_4_5_2.Team5.domain.user.dto.*;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Rq rq;

    @PostMapping("/signup")
    public RsData<UserDto> createUser(@RequestBody @Valid SignUpUserForm userForm) {

        User user = userService.createUser(userForm.username(), userForm.password(), userForm.email(),
                userForm.nickname(), userForm.address(), userForm.profileUrl());

        return new RsData<>(
                "201-1",
                "회원 가입이 완료되었습니다.",
                new UserDto(user)
        );
    }

    @PostMapping("/login")
    public RsData<LoginUserDto> loginUser(@RequestBody @Valid LoginUserForm userForm) {

        User user = userService.loginUser(userForm.username(), userForm.password());

        String accessToken = userService.generateAccessToken(user);
        rq.addCookie("accessToken", accessToken);
        rq.addCookie("refreshToken", user.getRefreshToken());

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(user.getNickname()),
                new LoginUserDto(
                        accessToken,
                        user.getRefreshToken(),
                        new UserDto(user)
                )
        );
    }

    @PostMapping("/logout")
    public RsData<Void> logoutUser() {

        User userIdentity = rq.getUserIdentity();
        userService.updateUserRefreshToken(userIdentity);

        // 사용자 인증 정보 제거
        rq.setLogout();

        // 쿠키 인증 정보 제거
        rq.removeCookie("accessToken");
        rq.removeCookie("refreshToken");

        return new RsData<>("200-1", "로그아웃 되었습니다.");
    }

    @GetMapping("/me")
    public RsData<UserDto> getCurrentUser() {

        User userIdentity = rq.getUserIdentity();
        User user = rq.getRealActor(userIdentity);

        return new RsData<>(
                "200-1",
                "내 정보 조회가 완료되었습니다.",
                new UserDto(user)
        );
    }

    @PostMapping("/refresh")
    public RsData<String> refreshAccessToken(@RequestBody @Valid RefreshUserForm userForm) {

        String refreshToken = userForm.refreshToken();
        User user = userService.getUserByRefreshToken(refreshToken)
                .orElseThrow(() -> new ServiceException("401-2", "유효하지 않은 RefreshToken입니다."));

        String newAccessToken = userService.generateAccessToken(user);
        rq.addCookie("accessToken", newAccessToken);
        rq.addCookie("refreshToken", refreshToken);

        return new RsData<>(
                "200-1",
                "AccessToken이 재발급되었습니다.",
                newAccessToken
        );
    }


}
