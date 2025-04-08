package com.NBE_4_5_2.Team5.domain.user.user.service.email.service

import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import jakarta.mail.internet.MimeMessage
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.bean.override.mockito.MockitoBean

/**
 * AuthTokenServiceTest 클래스는 이메일 서비스의 인증 코드 발송 및 존재하지 않는 이메일 처리를 테스트합니다.
 */
@SpringBootTest
@BaseTestConfig
class MailServiceTest {

    @MockitoBean
    private lateinit var mailSender: JavaMailSender           // 이메일 발송 기능 모킹

    @MockitoBean
    private lateinit var bouncedEmailService: BouncedEmailService   // 반송 이메일 검증 서비스 모킹

    @Autowired
    private lateinit var emailService: EmailService           // 테스트 대상 서비스

    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate   // Redis 객체

    private val testEmail = "test@example.com"               // 테스트용 이메일 주소
    private val redisKey = "email:$testEmail"                // 이메일 인증 코드 저장을 위한 Redis 키

    /**
     * 각 테스트 실행 전 Redis에 테스트용 인증 코드를 설정합니다.
     */
    @BeforeEach
    fun setUp() {
        // 테스트용 이메일 인증 코드 저장
        redisTemplate.opsForValue().set(redisKey, "123456")
    }

    /**
     * 유효한 이메일에 인증 코드 발송 성공 시나리오를 테스트합니다.
     */
    @Test
    @DisplayName("인증코드 발송 : 성공 : 유효한 이메일")
    fun test1() {
        // Given: 가짜 MimeMessage 객체 생성 및 mailSender 모킹 설정
        val mimeMessage = mock(MimeMessage::class.java)  // 가짜 이메일 객체 생성
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When: testEmail에 대해 인증 코드를 발송
        emailService.sendAuthenticationCode(testEmail)

        // Then: mailSender.send()가 한 번 호출되고, Redis에 인증 코드가 저장되어야 함
        verify(mailSender, times(1)).send(any(MimeMessage::class.java))
        val savedCode = redisTemplate.opsForValue().get("email:$testEmail")  // Redis에서 인증 코드 조회
        assertNotNull(savedCode)
    }

    /**
     * 존재하지 않는 이메일 주소에 대한 예외 처리를 테스트합니다.
     */
    @Test
    @DisplayName("존재하지 않는 이메일 주소에 대한 예외 처리")
    fun test2() {
        // Given: bouncedEmailService가 testEmail에 대해 false 반환 (이메일이 존재하지 않다고 가정)
        `when`(bouncedEmailService.checkBouncedEmail(testEmail)).thenReturn(false)

        // When & Then: checkBouncedEmail 호출 시 ServiceException 예외 발생 여부 검증
        val exception = assertThrows(ServiceException::class.java) {
            emailService.checkBouncedEmail(testEmail)
        }

        // Then: 예외 코드와 메시지 검증 & Redis에서 해당 이메일 키가 삭제되었는지 확인
        assertAll(
            { assertEquals("404-1", exception.code) },
            { assertEquals("존재하지않는 이메일입니다.", exception.message) }
        )
        Assertions.assertThat(redisTemplate.hasKey(redisKey)).isFalse()
    }
}
