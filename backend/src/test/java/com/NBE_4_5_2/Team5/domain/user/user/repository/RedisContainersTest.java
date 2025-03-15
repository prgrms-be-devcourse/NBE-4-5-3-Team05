package com.NBE_4_5_2.Team5.domain.user.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken;
import com.NBE_4_5_2.Team5.domain.user.user.repository.RedisRepository;
import com.NBE_4_5_2.Team5.global.config.RedisTestContainerConfig;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "custom.refreshToken.expire-seconds=3600")
class RedisContainersTest extends RedisTestContainerConfig {

	@Autowired
	private RedisRepository redisRepository;

	@Test
	@DisplayName("RefreshToken을 저장하고 조회할 수 있어야 한다.")
	void saveAndFindToken() {
		// Given
		String userId = "user123";
		String refreshToken = "refresh-token-abc";
		Long expiration = 3600L;

		RefreshToken token = RefreshToken.builder()
			.userId(userId)
			.refreshToken(refreshToken)
			.expiration(expiration)
			.build();

		// When
		redisRepository.save(token);
		Optional<RefreshToken> foundToken = redisRepository.findById(userId);

		// Then
		assertThat(foundToken).isPresent();
		assertThat(foundToken.get().getUserId()).isEqualTo(userId);
		assertThat(foundToken.get().getRefreshToken()).isEqualTo(refreshToken);
		assertThat(foundToken.get().getExpiration()).isEqualTo(expiration);
	}

	@Test
	@DisplayName("저장된 RefreshToken이 삭제되어야 한다.")
	void shouldDeleteRefreshToken() {
		// Given
		String userId = "userToDelete";
		RefreshToken token = RefreshToken.builder()
			.userId(userId)
			.refreshToken("token-to-delete")
			.expiration(3600L)
			.build();

		redisRepository.save(token);

		// When
		redisRepository.deleteById(userId);
		Optional<RefreshToken> foundToken = redisRepository.findById(userId);

		// Then
		assertThat(foundToken).isEmpty(); // 삭제 후 조회하면 없어야 함
	}
}

