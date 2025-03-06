package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.dto.UserUpdateRequest;
import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import com.NBE_4_5_2.Team5.global.security.SecurityUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public User createUser(String username, String password, String email,
                           String nickname, String address, String profileUrl) {

        userValidator.duplicate(username, email, nickname);

        User user = User.builder()
                .id("user-" + UUID.randomUUID())
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


    public User loginUser(String username, String password) {
        return userValidator.credentials(username, password);
    }

    public void logoutUser(User user) {
        String newRefreshToken = "user-" + UUID.randomUUID();
        user.setRefreshToken(newRefreshToken);

        userRepository.save(user);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    /**
     * 이 메소드는 AccessToken payload에 저장된 id와 username, role만을 가진 User 객체를 반환합니다.
     * DB 조회를 하지 않기 때문에, 관리자 페이지, 게시글 조회 등 사용자 id 혹은 role을 필요로 하는 경우에 사용할 수 있습니다.
     * <p>
     * CustomAuthenticationFilter에서 accessToken을 검증하고 setLogin 하는 과정에 사용됩니다.
     */
    public Optional<User> getUserByAccessToken(String accessToken) {

        Map<String, Object> payload = authTokenService.getPayload(accessToken);

        if (payload == null) {
            return Optional.empty();
        }

        String id = (String) payload.get("id");
        String username = (String) payload.get("username");
        Role role = (Role) payload.get("role");

        return Optional.of(
                User.builder()
                        .id(id)
                        .username(username)
                        .role(role)
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

    public User getUserIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new ServiceException("401-2", "로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof SecurityUser)) {
            throw new ServiceException("401-3", "잘못된 인증 정보입니다");
        }

        SecurityUser user = (SecurityUser) principal;

        return User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
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
