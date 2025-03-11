package com.NBE_4_5_2.Team5.domain.user.service;

import com.NBE_4_5_2.Team5.global.config.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
@SpringBootTest
class EmailServiceTest extends BaseTest {
    @Autowired
    private EmailService emailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void 이메일_인증_코드_발송_테스트() {
        // given
        String testEmail = "ok6737@naver.com";

        // when
        emailService.sendAuthenticationCode(testEmail);

        // then
        String code = redisTemplate.opsForValue().get("email:" + testEmail);
        assertNotNull(code);
        System.out.println("발송된 인증 코드: " + code);
    }
}