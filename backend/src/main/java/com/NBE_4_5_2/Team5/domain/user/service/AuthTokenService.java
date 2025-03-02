package com.NBE_4_5_2.Team5.domain.user.service;

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

    String generateAccessToken(User user) {

        return Ut.Jwt.createToken(
                keyString,
                expireSeconds,
                Map.of("id", user.getId(), "username", user.getUsername())
        );
    }

    Map<String, Object> getPayload(String token) {
        Map<String, Object> payload = Ut.Jwt.getPayload(keyString, token);

        if(payload == null) return null;

        String id = (String)payload.get("id");
        String username = (String)payload.get("username");

        return Map.of("id", id, "username", username);
    }
}
