package com.NBE_4_5_2.Team5.domain.user.user.repository

import com.NBE_4_5_2.Team5.domain.user.user.entity.RefreshToken
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.*

/**
 * RedisContainersTest 클래스는 Redis TestContainers 환경에서
 * Redis 관련 기능이 정상 동작하는지 테스트합니다.
 */
@SpringBootTest
@BaseTestConfig
class RedisContainersTest {

    @Autowired
    private lateinit var redisRepository: RedisRepository

    @Autowired
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    /**
     * Redis TestContainers의 기본 동작을 확인하는 테스트입니다.
     */
    @Test
    @DisplayName("redis : TestContainers 정상 동작 확인")
    fun test1() {
        // Given&When: Redis에 ping 명령어 실행
        val pingResponse = redisTemplate.execute { connection -> connection.ping() }

        // Then: 응답이 "PONG"인지 확인
        assertThat(pingResponse).isEqualTo("PONG")
    }

    /**
     * StringRedisTemplate과 RedisTemplate이 동일한 ConnectionFactory를 사용하는지 확인하는 테스트입니다.
     */
    @Test
    @DisplayName("redis : 동일한 ConnectionFactory를 사용하는지 확인")
    fun test2() {
        // Then: 두 템플릿의 ConnectionFactory가 동일한지 검증
        assertThat(redisTemplate.connectionFactory)
            .isSameAs(stringRedisTemplate.connectionFactory)
    }

    /**
     * StringRedisTemplate을 사용하여 데이터를 저장 및 조회하는 테스트입니다.
     */
    @Test
    @DisplayName("redis : StringRedisTemplate : 저장 및 조회 테스트")
    fun test3() {
        // Given: 테스트용 key와 value 설정
        val key = "test-key"           // 테스트 키
        val value = "test-value"       // 테스트 값

        // When: StringRedisTemplate으로 값 저장 후 조회
        stringRedisTemplate.opsForValue().set(key, value)
        val storedValue = stringRedisTemplate.opsForValue().get(key)

        // Then: 저장된 값이 입력값과 일치하는지 확인
        assertThat(storedValue).isEqualTo(value)
    }

    /**
     * RedisTemplate을 사용하여 데이터를 저장 및 조회하는 테스트입니다.
     */
    @Test
    @DisplayName("redis : RedisTemplate : 저장 및 조회 테스트")
    fun test4() {
        // Given: RedisTemplate용 key와 value 설정
        val key = "redis-template-key"               // RedisTemplate 테스트 키
        val value = "redis-template-value"             // RedisTemplate 테스트 값

        // When: 값 저장 후 조회
        redisTemplate.opsForValue().set(key, value)
        val storedValue = redisTemplate.opsForValue().get(key)

        // Then: 저장된 값이 일치하는지 검증
        assertThat(storedValue).isEqualTo(value)
    }

    /**
     * RedisRepository를 사용하여 RefreshToken을 저장하고 조회하는 테스트입니다.
     */
    @Test
    @DisplayName("redis : RedisRepository : RefreshToken 저장 조회 테스트")
    fun test5() {
        // Given: 테스트용 userId, refreshToken, 만료기간 설정
        val userId = "user123"
        val refreshToken = "refresh-token-abc"
        val expiration: Long = 3600L
        val token = RefreshToken(userId, refreshToken, expiration)  // RefreshToken 객체 생성

        // When: 토큰 저장 후 userId로 조회
        redisRepository.save(token)
        val foundToken: Optional<RefreshToken> = redisRepository.findById(userId)

        // Then: 조회된 토큰이 존재하며, 필드 값들이 올바른지 검증
        assertThat(foundToken).isPresent
        assertThat(foundToken.get().userId).isEqualTo(userId)
        assertThat(foundToken.get().refreshToken).isEqualTo(refreshToken)
        assertThat(foundToken.get().expiration).isEqualTo(expiration)
    }

    /**
     * RedisRepository, StringRedisTemplate, RedisTemplate이 동일한 Redis 인스턴스를 사용하는지 확인하는 테스트입니다.
     */
    @Test
    @DisplayName("redis : RedisRepository, StringRedisTemplate, RedisTemplate : 같은 Redis를 사용하는지 확인")
    fun test6() {
        // Given: 공유 key와 value 설정
        val key = "shared-key"
        val value = "shared-value"

        // When: StringRedisTemplate으로 값 저장 후 RedisTemplate으로 조회
        stringRedisTemplate.opsForValue().set(key, value)
        val redisTemplateValue = redisTemplate.opsForValue().get(key)

        // Then: 두 템플릿 모두 저장된 값이 일치하는지 검증
        assertThat(redisTemplateValue).isEqualTo(value)
        assertThat(stringRedisTemplate.opsForValue().get(key)).isEqualTo(value)
    }

    /**
     * RedisRepository를 사용하여 RefreshToken을 삭제하는 기능을 테스트합니다.
     */
    @Test
    @DisplayName("redis : redisRepository : RefreshToken 삭제 테스트")
    fun test7() {
        // Given: 삭제 대상 userId 설정 및 RefreshToken 객체 생성
        val userId = "userToDelete"                    // 삭제할 userId
        val token = RefreshToken(userId, "token-to-delete", 3600L)

        // When: 토큰 저장 후 삭제, 그리고 조회
        redisRepository.save(token)
        redisRepository.deleteById(userId)
        val foundToken = redisRepository.findById(userId)

        // Then: 조회 결과가 비어있음을 검증
        assertThat(foundToken).isEmpty
    }

    /**
     * RedisRepository를 사용하여 refreshToken으로 조회하는 기능을 테스트합니다.
     */
    @Test
    @DisplayName("redis : redisRepository : RefreshToken으로 조회 테스트")
    fun test8() {
        // Given: 조회용 테스트 데이터 설정
        val userId = "userWithRefreshToken"            // 테스트 userId
        val refreshToken = "unique-refresh-token"      // 고유 refresh token
        val token = RefreshToken(userId, refreshToken, 3600L)

        // When: 토큰 저장 후 refreshToken으로 조회
        redisRepository.save(token)
        val foundToken = redisRepository.findByRefreshToken(refreshToken)

        // Then: 조회된 토큰이 존재하며, 필드 값들이 일치하는지 검증
        assertThat(foundToken).isPresent
        assertThat(foundToken.get().userId).isEqualTo(userId)
        assertThat(foundToken.get().refreshToken).isEqualTo(refreshToken)
    }
}
