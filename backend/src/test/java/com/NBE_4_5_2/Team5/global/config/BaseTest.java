package com.NBE_4_5_2.Team5.global.config;

import com.NBE_4_5_2.Team5.domain.user.service.EmailService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@Import(TestConfig.class)
@Transactional
@TestPropertySource(properties = {
        "custom.refreshToken.expire-seconds=3600",
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "spring.mail.username=test",
        "spring.mail.password=test",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "spring.mail.properties.mail.smtp.starttls.required=false",
        "spring.mail.properties.mail.smtp.connectiontimeout=5000",
        "spring.mail.properties.mail.smtp.timeout=5000",
        "spring.mail.properties.mail.smtp.writetimeout=5000",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
public abstract class BaseTest {

    @MockitoBean
    protected EmailService emailService;

    @MockitoBean
    protected JavaMailSender javaMailSender;

    @MockitoBean
    protected StringRedisTemplate redisTemplate;

    @AfterAll
    static void stopRedisContainer() {
        TestConfig.stopContainer();
    }
}
