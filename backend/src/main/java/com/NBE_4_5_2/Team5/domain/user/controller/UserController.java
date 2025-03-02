package com.NBE_4_5_2.Team5.domain.user.controller;

import com.NBE_4_5_2.Team5.domain.user.dto.SignUpUserForm;
import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @PostMapping("/signup")
    public RsData<UserDto> signup(@RequestBody @Valid SignUpUserForm userForm) {

        User user = userService.signup(userForm.username(), userForm.password(), userForm.email(),
                userForm.nickname(), userForm.address(), userForm.profileUrl());

        return new RsData<>(
                "201-1",
                "회원 가입이 완료되었습니다.",
                new UserDto(user)
        );
    }

    record LoginUserForm(@NotBlank(message = "아이디는 필수 입력값입니다.") String username,
                         @NotBlank(message = "비밀번호는 필수 입력값입니다.") String password) {
    }

    record LoginUserDto(String refreshToken, UserDto item) {
    }

    @PostMapping("/login")
    public RsData<LoginUserDto> login(@RequestBody @Valid LoginUserForm reqBody) {

        User user = userService.findByUsername(reqBody.username()).orElseThrow(
                () -> new ServiceException("401-1", "잘못된 아이디입니다.")
        );

        if (!user.getPassword().equals(reqBody.password())) {
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(user.getNickname()),
                new LoginUserDto(
                        user.getRefreshToken(),
                        new UserDto(user)
                )
        );
    }


}
