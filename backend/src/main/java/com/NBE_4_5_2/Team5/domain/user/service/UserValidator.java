package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void duplicate(String username, String email, String nickname) {
        // TODO : select 3번 발생 최적화 필요
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                });

        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new ServiceException("409-2", "이미 사용중인 이메일입니다.");
                });

        userRepository.findByNickname(nickname)
                .ifPresent(user -> {
                    throw new ServiceException("409-3", "이미 사용중인 닉네임입니다.");
                });

    }

    public User credentials(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("401-1", "잘못된 아이디입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }
        return user;

    }
}
