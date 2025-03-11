package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.global.config.RedisTestContainerConfig;
import com.NBE_4_5_2.Team5.global.standard.util.Ut;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Import(RedisTestContainerConfig.class)
@TestPropertySource(properties = "custom.refreshToken.expire-seconds=3600")
public class AuthTokenServiceTest {

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private UserService userService;

    @Value("${custom.jwt.secret-key}")
    private String keyString;

    @AfterAll
    static void stopRedisContainer() {
        RedisTestContainerConfig.stopContainer();
    }

    @Test
    @DisplayName("user1 - accessToken 생성 성공")
    void accessToken() {
        // Given
        User user = userService.getUserByUsername("user1").orElseThrow();

        // When
        String accessToken = authTokenService.generateAccessToken(user);

        // Then
        assertThat(accessToken).isNotBlank();
        System.out.println("accessToken = " + accessToken);
    }

    @Test
    @DisplayName("jwt 유효성 체크")
    void checkValid() {
        // Given
        User user = userService.getUserByUsername("user1").orElseThrow();
        String accessToken = authTokenService.generateAccessToken(user);

        // When
        boolean isValid = Ut.Jwt.isValidToken(keyString, accessToken);
        Map<String, Object> parsedPayload = authTokenService.getPayload(accessToken);

        // Then
        assertThat(isValid).isTrue();
        assertThat(parsedPayload).containsAllEntriesOf(
                Map.of("id", user.getId(), "username", user.getUsername())
        );
    }
}
