package com.NBE_4_5_2.Team5.domain.user.user.service.email

import com.NBE_4_5_2.Team5.domain.user.user.service.email.service.BouncedEmailService
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import jakarta.mail.MessagingException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.Duration
import kotlin.random.Random

/**
 * 이메일 전송 및 인증 관련 서비스를 제공하는 클래스
 * 이메일 전송, 인증 코드 생성 및 검증, 그리고 반송 이메일(존재하지 않는 이메일로 발송된 경우) 확인 후 처리 기능을 제공합니다.
 *
 * @property mailSender JavaMailSender를 통해 이메일 메시지를 생성하고 전송
 * @property redisTemplate Redis를 이용해 인증 코드를 저장하고 관리
 * @property bouncedEmailService 반송 이메일 확인 처리를 위한 서비스를 제공
 */
@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val redisTemplate: StringRedisTemplate,
    private val bouncedEmailService: BouncedEmailService
) {
    @Value("\${spring.mail.properties.auth-code-expiration-second}")
    private var expireSeconds: Long = 0

    companion object {
        private const val EMAIL_KEY = "email:"
    }

    /**
     * 랜덤한 6자리 인증 코드를 생성하여 반환합니다.
     *
     * @return 6자리 형식의 인증 코드 (예: "035612")
     */
    private val randomCode: String
        get() = Random.nextInt(0, 1_000_000)
            .toString()
            .padStart(6, '0')

    /**
     * 주어진 이메일 주소로 인증 코드가 포함된 HTML 이메일을 전송합니다.
     *
     * 이메일 본문은 "templates/email/email-template.html" 템플릿 파일을 사용하며,
     * 전송 후 인증 코드는 email과 함께 Redis에 저장됩니다.
     *
     * @param email 인증 코드를 전송할 대상 이메일 주소
     * @throws ServiceException 템플릿 로딩 또는 이메일 전송 중 문제가 발생한 경우 예외를 발생
     */
    fun sendAuthenticationCode(email: String) {
        try {
            val code = randomCode
            val subject = "요청하신 이메일 인증 코드는 $code 입니다."
            val htmlContent = loadHtmlTemplate("templates/email/email-template.html")
                .replace("{{CODE}}", code)

            sendHtmlEmail(email, subject, htmlContent) // HTML 형식의 이메일 발송
            saveVerificationCode(email, code) // 인증 코드를 Redis에 저장
        } catch (e: IOException) {
            throw ServiceException("500-1", "이메일 발송 템플릿이 존재하지 않습니다.")
        } catch (e: MessagingException) {
            throw ServiceException("500-2", "이메일 전송에 실패했습니다.")
        }
    }

    /**
     * HTML 형식의 이메일 메시지를 전송합니다.
     *
     * 메시지 생성과 전송 과정에서 문제가 발생하면 MessagingException을 던집니다.
     *
     * @param email 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param htmlContent HTML 형식의 이메일 본문
     * @throws MessagingException 이메일 전송 중 오류가 발생한 경우
     */
    @Throws(MessagingException::class)
    private fun sendHtmlEmail(email: String, subject: String, htmlContent: String) {
        val message = mailSender.createMimeMessage() // 메시지 객체 생성

        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(email)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)

        mailSender.send(message)
    }

    /**
     * HTML 템플릿 파일을 읽어 문자열로 반환합니다.
     *
     * @param path 파일 경로 ("templates/email/email-template.html")
     * @return 템플릿 파일의 내용 문자열
     * @throws IOException 템플릿 파일을 읽어오는 중 문제가 발생한 경우
     */
    @Throws(IOException::class)
    private fun loadHtmlTemplate(path: String): String {
        val resource = ClassPathResource(path)
        return String(Files.readAllBytes(resource.file.toPath()), StandardCharsets.UTF_8)
    }

    /**
     * 반송 이메일이 존재하는지 확인하여, 존재하면 Redis에 저장된 해당 인증 코드를 삭제하고 예외를 발생시킵니다.
     *
     * [BouncedEmailService.checkBouncedEmail]의 결과가 false일 경우(즉, 반송 이메일이 존재하는 경우)
     * Redis에 저장된 인증 코드를 삭제한 후 ServiceException을 던집니다.
     *
     * @param email 반송 여부를 확인할 대상 이메일 주소
     * @throws ServiceException 반송 이메일이 확인된 경우 "이메일 발송에 실패했습니다." 메시지와 함께 예외 발생
     */
    fun checkBouncedEmail(email: String) {
        bouncedEmailService.checkBouncedEmail(email)
            .takeIf { !it }
            .also {
                redisTemplate.delete(createEmailKey(email))
                throw ServiceException("404-1", "존재하지않는 이메일입니다.")
            }
    }

    /**
     * 인증코드 발송 여부를 확인하기 위해
     * Redis에 해당 이메일을 키로 저장한 인증 코드가 존재하는지 확인합니다
     *
     * @param email 인증 코드가 저장된 대상 이메일 주소
     * @return 저장된 인증 코드 문자열, 없으면 null 반환
     */
    fun getVerificationCode(email: String): String? {
        val key = createEmailKey(email)
        return redisTemplate.opsForValue()[key]
    }

    /**
     * 사용자가 입력한 인증 코드와 저장된 인증 코드가 일치하는지 검증합니다.
     *
     * @param code 사용자가 입력한 인증 코드
     * @param savedCode Redis에 저장된 인증 코드
     * @return 인증 코드가 일치하면 true, 불일치하면 false
     */
    fun verifyAuthenticationCode(code: String, savedCode: String): Boolean {
        return code == savedCode
    }

    /**
     * 주어진 이메일 주소와 인증 코드를 Redis에 저장합니다.
     *
     * 저장된 인증 코드는 expireSeconds 설정에 따라 자동 만료됩니다.
     *
     * @param email 인증 코드를 저장할 대상 이메일 주소
     * @param code 저장할 인증 코드
     */
    fun saveVerificationCode(email: String, code: String) {
        val key = createEmailKey(email)
        redisTemplate.opsForValue()[key, code] = Duration.ofSeconds(expireSeconds)
    }

    /**
     * 주어진 이메일 주소를 이용하여 Redis 저장에 사용할 키를 생성합니다.
     *
     * @param email 대상 이메일 주소
     * @return 생성된 Redis 키 (예: "email:example@domain.com")
     */
    private fun createEmailKey(email: String): String = EMAIL_KEY + email
}