package com.NBE_4_5_2.Team5.domain.user.user.service.email.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.NBE_4_5_2.Team5.global.config.RedisTestContainerConfig;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;

import jakarta.mail.internet.MimeMessage;

@SpringBootTest
@BaseTestConfig
class EmailServiceTest extends RedisTestContainerConfig {

	@MockitoBean
	private JavaMailSender mailSender;
	@MockitoBean
	private BouncedEmailService bouncedEmailService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private StringRedisTemplate redisTemplate;

	private final String testEmail = "test@example.com";
	private final String redisKey = "email:" + testEmail;

	@BeforeEach
	void setUp() {
		// 테스트용 이메일 인증 코드
		redisTemplate.opsForValue().set(redisKey, "123456");
	}

	@Test
	@DisplayName("인증코드 발송 : 성공 : 유효한 이메일")
	void test1() {
		// given
		MimeMessage mimeMessage = mock(MimeMessage.class); // 가짜 이메일 객체 생성
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		// when
		emailService.sendAuthenticationCode(testEmail);

		// then
		verify(mailSender, times(1)).send(any(MimeMessage.class));

		// Redis에 인증 코드가 저장되었는지 확인
		String savedCode = redisTemplate.opsForValue().get("email:" + testEmail);
		assertNotNull(savedCode);
	}

	@Test
	@DisplayName("인증코드 발송 : 실패 : 존재하지 않는 이메일")
	void test2() {
		// given: 반송된 이메일이라면 false 반환하도록 설정
		when(bouncedEmailService.checkBouncedEmail(testEmail)).thenReturn(false);

		// when & then: 예외가 발생하는지 확인
		ServiceException exception = assertThrows(ServiceException.class, () ->
			emailService.checkBouncedEmail(testEmail)
		);

		// 예외 메시지 검증
		assertAll(
			() -> assertEquals("404-1", exception.getCode()),
			() -> assertEquals("존재하지않는 이메일입니다.", exception.getMessage())
		);

		// 실제 Redis에서 값이 삭제되었는지 확인
		Assertions.assertThat(redisTemplate.hasKey(redisKey)).isFalse();
	}
}
