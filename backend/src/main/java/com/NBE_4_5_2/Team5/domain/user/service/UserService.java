package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.Rq;
import lombok.RequiredArgsConstructor;
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

    public void updateUserRefreshToken(User userIdentity) {

        User user = rq.getRealActor(userIdentity);

        String newRefreshToken = UUID.randomUUID().toString();
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

    /*
         이 메소드는 AccessToken payload에 저장된 id와 username만을 가진 User 객체를 반환합니다.
         DB 조회를 하지 않기 때문에, 게시글 조회 등 사용자 id 혹은 username만 필요로 하는 경우에 효율적으로 사용할 수 있습니다.
    */
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

}
