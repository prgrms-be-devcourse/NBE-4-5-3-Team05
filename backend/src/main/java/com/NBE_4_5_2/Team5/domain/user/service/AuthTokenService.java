package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.domain.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.global.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    @Value("${custom.jwt.secret-key}")
    private String keyString;

    @Value("${custom.jwt.expire-seconds}")
    private int expireSeconds;

    // id, username, role 정보를 담은 accessToken 생성
    String generateAccessToken(User user) {

        return Ut.Jwt.createToken(
                keyString,
                expireSeconds,
                Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "role", user.getRole().name()
                )
        );
    }

    Map<String, Object> getPayload(String accessToken) {

        if (!Ut.Jwt.isValidToken(keyString, accessToken)) {
            return null;
        }

        Map<String, Object> payload = Ut.Jwt.getPayload(keyString, accessToken);

        String id = (String) payload.get("id");
        String username = (String) payload.get("username");
        String roleStr = (String) payload.get("role");
        Role role = Role.valueOf(roleStr);

        return Map.of(
                "id", id,
                "username", username,
                "role", role
        );
    }
}
