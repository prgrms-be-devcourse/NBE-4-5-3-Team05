package com.NBE_4_5_2.Team5.domain.user.repository;

import com.NBE_4_5_2.Team5.domain.user.entity.RefreshToken;
import com.NBE_4_5_2.Team5.global.config.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisContainersTest extends BaseTest {

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("TestContainers Redis 정상 동작 확인")
    void redisTestContainersIsRunning() {
        String pingResponse = redisTemplate.execute(RedisConnectionCommands::ping);
        assertThat(pingResponse).isEqualTo("PONG");
    }

    @Test
    @DisplayName("RedisConnectionFactory가 동일한지 확인")
    void testRedisConnectionFactory() {
        assertThat(redisTemplate.getConnectionFactory()).isSameAs(stringRedisTemplate.getConnectionFactory());
    }

    @Test
    @DisplayName("StringRedisTemplate을 저장 및 조회 테스트")
    void testStringRedisTemplate() {
        // Given
        String key = "test-key";
        String value = "test-value";

        // When
        stringRedisTemplate.opsForValue().set(key, value);
        String storedValue = stringRedisTemplate.opsForValue().get(key);

        // Then
        assertThat(storedValue).isEqualTo(value);
    }

    @Test
    @DisplayName("redis : RedisTemplate : 저장 및 조회 테스트")
    void testRedisTemplate() {
        // Given
        String key = "redis-template-key";
        String value = "redis-template-value";

        // When
        redisTemplate.opsForValue().set(key, value);
        String storedValue = redisTemplate.opsForValue().get(key);

        // Then
        assertThat(storedValue).isEqualTo(value);
    }

    @Test
    @DisplayName("redis : RedisRepository : RefreshToken 저장 조회 테스트")
    void test1() {
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
    @DisplayName("RedisRepository, StringRedisTemplate, RedisTemplate이 같은 Redis를 사용하는지 확인")
    void testRedisSharedStorage() {
        // Given
        String key = "shared-key";
        String value = "shared-value";

        // StringRedisTemplate으로 저장
        stringRedisTemplate.opsForValue().set(key, value);

        // RedisTemplate으로 조회
        String redisTemplateValue = redisTemplate.opsForValue().get(key);

        // RedisRepository에서 확인
        Optional<RefreshToken> foundToken = redisRepository.findById(key);

        // Then
        assertThat(redisTemplateValue).isEqualTo(value); // RedisTemplate 조회
        assertThat(stringRedisTemplate.opsForValue().get(key)).isEqualTo(value); // StringRedisTemplate 조회
    }

    @Test
    @DisplayName("redis : RefreshToken 삭제 테스트")
    void test2() {
        // Given
        String userId = "userToDelete";


        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .refreshToken("token-to-delete")
                .expiration(3600L)
                .build();

        // When
        redisRepository.save(token);
        redisRepository.deleteById(userId);
        Optional<RefreshToken> foundToken = redisRepository.findById(userId);

        // Then
        assertThat(foundToken).isEmpty();
    }

    @Test
    @DisplayName("redis : RefreshToken으로 조회 테스트")
    void test3() {
        String userId = "userWithRefreshToken";
        String refreshToken = "unique-refresh-token";
        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .expiration(3600L)
                .build();

        redisRepository.save(token);

        Optional<RefreshToken> foundToken = redisRepository.findByRefreshToken(refreshToken);

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getUserId()).isEqualTo(userId);
        assertThat(foundToken.get().getRefreshToken()).isEqualTo(refreshToken);
    }
}
