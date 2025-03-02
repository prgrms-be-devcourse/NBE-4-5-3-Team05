package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;

    public User signup(String username, String password, String email,
                       String nickname, String address, String profileUrl) {

        validateDuplicateUser(username, email, nickname);


        User user = User.builder()
                .id("user-" + UUID.randomUUID().toString())
                .username(username)
                .refreshToken(UUID.randomUUID().toString())
                .password(password)
                .email(email)
                .nickname(nickname)
                .address(address)
                .profileUrl(profileUrl)
                .build();

        return userRepository.save(user);
    }

    public void validateDuplicateUser(String username, String email, String nickname) {
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

    public long count() {
        return userRepository.count();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
