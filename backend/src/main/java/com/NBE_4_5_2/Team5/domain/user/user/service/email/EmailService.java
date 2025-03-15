package com.NBE_4_5_2.Team5.domain.user.user.service.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.user.user.service.email.service.BouncedEmailService;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private static final String EMAIL_KEY = "email:";

	@Value("${spring.mail.properties.auth-code-expiration-second}")
	private Long expireSeconds;

	private final JavaMailSender mailSender;
	private final StringRedisTemplate redisTemplate;
	private final BouncedEmailService bouncedEmailService;

	// 이메일 인증 코드 전송
	public void sendAuthenticationCode(String email) {

		try {
			String code = generateVerificationCode();
			String subject = "요청하신 이메일 인증 코드는 %s입니다.".formatted(code);
			String htmlContent = loadHtmlTemplate("templates/email/email-template.html").replace("{{CODE}}", code);

			sendHtmlEmail(email, subject, htmlContent);
			saveVerificationCode(email, code); // 인증 코드 Redis 저장

		} catch (IOException e) {
			throw new ServiceException("500-1", "이메일 발송 템플릿이 존재하지 않습니다.");
		} catch (MessagingException e) {
			throw new ServiceException("500-2", "이메일 전송에 실패했습니다.");
		}
	}

	private void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setTo(toEmail);
		helper.setSubject(subject);
		helper.setText(htmlContent, true);

		mailSender.send(message);
	}

	private String loadHtmlTemplate(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path);
		return new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
	}

	// 존재하지 않는 이메일로 발송된 경우 예외 처리 및 redis에 저장된 메일을 삭제
	public void checkBouncedEmail(String email) {
		if (!bouncedEmailService.checkBouncedEmail(email)) {
			String key = EMAIL_KEY + email;
			redisTemplate.delete(key);
			throw new ServiceException("404-1", "존재하지않는 이메일입니다.");
		}
	}

	// 인증 코드 생성
	private String generateVerificationCode() {
		int random = (int)(Math.random() * 1_000_000); // 0 ~ 999999
		return String.format("%06d", random);
	}

	public String getVerificationCode(String email) {
		String key = EMAIL_KEY + email;
		return redisTemplate.opsForValue().get(key);
	}

	public boolean verifyAuthenticationCode(String code, String savedCode) {
		return code.equals(savedCode);
	}

	public void saveVerificationCode(String email, String code) {
		String key = EMAIL_KEY + email;
		redisTemplate.opsForValue().set(key, code, Duration.ofSeconds(expireSeconds));
	}

	public void deleteVerificationCode(String email) {
		String key = EMAIL_KEY + email;
		redisTemplate.delete(key);
	}

	public boolean existsEmail(String email) {
		String key = EMAIL_KEY + email;
		return redisTemplate.hasKey(key);
	}

}
