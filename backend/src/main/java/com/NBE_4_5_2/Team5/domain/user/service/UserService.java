package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.dto.UserUpdateRequest;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final Rq rq;

    public User signup(String username, String password, String email,
                       String nickname, String address, String profileUrl) {

        userValidator.duplicate(username, email, nickname);

        User user = User.builder()
                .id("user-" + UUID.randomUUID().toString())
                .username(username)
                .refreshToken(UUID.randomUUID().toString())
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .address(address)
                .profileUrl(profileUrl)
                .build();

        return userRepository.save(user);
    }

    public void logout(User user) {
        String newRefreshToken = "user-" + UUID.randomUUID();
        user.setRefreshToken(newRefreshToken);

        userRepository.save(user);
    }

    public User processUserAuthentication(String username, String password) {
        return userValidator.credentials(username, password);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    // AccessToken을 통해 DB 조회를 하지않고 id와 username 만을 가진 User 객체를 반환
    public Optional<User> getUserByAccessToken(String accessToken) {

        Map<String, Object> payload = authTokenService.getPayload(accessToken);

        if (payload == null) {
            return Optional.empty();
        }

        String id = (String) payload.get("id");
        String username = (String) payload.get("username");

        return Optional.of(
                User.builder()
                        .id(id)
                        .username(username)
                        .build()
        );
    }

    public String getAuthToken(User user) {
        return user.getRefreshToken() + " " + authTokenService.generateAccessToken(user);
    }

    public String generateAccessToken(User user) {
        return authTokenService.generateAccessToken(user);
    }

    public long count() {
        return userRepository.count();
    }

    // 내 프로필 조회
    public UserDto getMyProfile(User user) {
        return UserDto.fromEntity(user);
    }

    // 내 프로필 수정
    @Transactional
    public UserDto updateMyProfile(User user, UserUpdateRequest updateRequest) {
        // 닉네임 변경
        if (updateRequest.getNickname() != null) {
            user.setNickname(updateRequest.getNickname());
        }

        // 주소 변경
        if (updateRequest.getAddress() != null) {
            user.setAddress(updateRequest.getAddress());
        }

        // 프로필 이미지 변경
        if (updateRequest.getProfileUrl() != null) {
            user.setProfileUrl(updateRequest.getProfileUrl());
        }

        // 이메일 변경 시 중복 체크
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new ServiceException("400-EMAIL-ALREADY-EXISTS", "이미 사용 중인 이메일입니다.");
            }
            user.setEmail(updateRequest.getEmail());
        }

        return UserDto.fromEntity(user);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMyProfile(User user) {
        userRepository.delete(user);
    }


}
