package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.global.standard.util.Ut;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {

    @Autowired
    private AuthTokenService authTokenService;
    @Autowired
    private UserService userService;

    @Value("${custom.jwt.secret-key}")
    private String keyString;

    @Test
    @DisplayName("user1 - accessToken 생성 성공")
    void accessToken() {

        User user = userService.findByUsername("user1").get();
        String accessToken = authTokenService.generateAccessToken(user);

        assertThat(accessToken).isNotBlank();

        System.out.println("accessToken = " + accessToken);
    }

    @Test
    @DisplayName("jwt 유효성 체크")
    void checkValid() {

        User user = userService.findByUsername("user1").get();
        String accessToken = authTokenService.generateAccessToken(user); // accessToken 생성
        boolean isValid = Ut.Jwt.isValidToken(keyString, accessToken);
        assertThat(isValid).isTrue();

        Map<String, Object> parsedPayload = authTokenService.getPayload(accessToken); // accessToken 역직렬화

        assertThat(parsedPayload).containsAllEntriesOf(
                Map.of("id", user.getId(), "username", user.getUsername()) // 정상 작동 확인
        );
    }
}