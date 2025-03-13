package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.service.email.EmailService;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void duplicate(String username, String nickname) {

        if (userRepository.existsByUsername(username)) {
            throw new ServiceException("400-USERNAME-ALREADY-EXISTS", "이미 사용중인 아이디입니다.");
        }

        if(userRepository.existsByNickname(nickname)) {
            throw new ServiceException("400-NICKNAME-ALREADY-EXISTS", "이미 사용중인 닉네임입니다.");
        }

    }

    public User credentials(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("401-1", "잘못된 아이디입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }
        return user;

    }

    public void emailVerified(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new ServiceException("400-EMAIL-ALREADY-EXISTS", "이미 사용중인 이메일입니다.");
        }

        // 해당 이메일에 대한 인증이 완료되었는지 검증
        String verificationCode = emailService.getVerificationCode(email);
        if (verificationCode == null || !verificationCode.equals("verified")) {
            throw new ServiceException("400-EMAIL-VERIFICATION-REQUIRED", "이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요.");
        }

    }
}
