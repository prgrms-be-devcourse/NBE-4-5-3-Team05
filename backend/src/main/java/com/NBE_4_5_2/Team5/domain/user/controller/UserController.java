package com.NBE_4_5_2.Team5.domain.user.controller;

import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    record SignUpUserForm(String username, String password, String email,
                          String nickname, String address, String profileUrl) {
    }

    @PostMapping("/signup")
    public RsData<UserDto> signup(@RequestBody SignUpUserForm userForm) {

        User user = userService.signup(userForm.username(), userForm.password(), userForm.email(),
                userForm.nickname(), userForm.address(), userForm.profileUrl());

        return new RsData<>(
                "201-1",
                "회원 가입이 완료되었습니다.",
                new UserDto(user)
        );
    }

}
