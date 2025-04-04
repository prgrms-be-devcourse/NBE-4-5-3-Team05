package com.NBE_4_5_2.Team5.domain.user.user.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.user.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.global.standard.util.Ut;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

	@Value("${custom.jwt.secret-key}")
	private String keyString;

	@Value("${custom.jwt.expire-seconds}")
	private int expireSeconds;

	String generateRefreshToken() {
		return UUID.randomUUID().toString();
	}

	// id, username, role 정보를 담은 accessToken 생성
	private final UserRepository userRepository;

	public String generateAccessToken(User user) {

		return Ut.Jwt.createToken(
			keyString,
			expireSeconds,
			Map.of(
				"id", user.getId(),
				"username", user.getUsername(),
				"nickname", user.getNickname(),
				"role", user.getRole().name()
			)
		);
	}

	public Map<String, Object> getPayload(String accessToken) {

		if (!Ut.Jwt.isValidToken(keyString, accessToken)) {
			return null;
		}

		Map<String, Object> payload = Ut.Jwt.getPayload(keyString, accessToken);

		String id = (String)payload.get("id");
		String username = (String)payload.get("username");
		String nickname = (String)payload.get("nickname");
		String roleStr = (String)payload.get("role");
		Role role = Role.valueOf(roleStr);

		return Map.of(
			"id", id,
			"username", username,
			"nickname", nickname,
			"role", role
		);
	}

	public String getUsernameFromToken(String accesstoken) {
		Map<String, Object> payload = getPayload(accesstoken);
		if (payload != null) {
			return getNicknameFromName((String)payload.get("username"));
		}
		return null;
	}

	public String getNicknameFromName(String username) {
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			return user.getNickname();
		}
		return null;
	}
}
