package com.NBE_4_5_2.Team5.domain.user.user.service.email.service

import com.NBE_4_5_2.Team5.global.config.email.TimeProvider
import com.NBE_4_5_2.Team5.global.config.email.properties.Pop3
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import jakarta.mail.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class BouncedEmailService(
    private val pop3: Pop3,
    private val timeProvider: TimeProvider
) {
    companion object {
        private const val FAILED_RECIPIENTS_HEADER = "X-Failed-Recipients"
    }

    /**
     * 발송된 이메일 주소가 존재하는지 확인합니다.
     * 메일함으로 반송 메일이 도착하기까지 대기하기 위해 sleep 적용.
     *
     * @param email 확인할 이메일 주소
     * @return 해당 이메일로 반송된 메일이 존재하면 false, 없으면 true
     */
    fun checkBouncedEmail(email: String): Boolean {
        try {
            // 반송 메일이 도착할 시간을 기다립니다 (3초)
            timeProvider.sleep(3000)
            val emailFolder = getEmailFolder()
            // 현재 시간 - untilTime (예를 들어 최근 2분 내 도착한 메일만 확인)
            val untilTime = LocalDateTime.now().minusSeconds(pop3.untilTime.toLong())

            for (message in emailFolder.messages) {
                val messageTime = getMessageTime(message)
                if (untilTime.isBefore(messageTime)) { // untilTime 이후에 도착한 메일이라면
                    message.getHeader(FAILED_RECIPIENTS_HEADER)?.let { recipients ->
                        if (recipients.isNotEmpty() && recipients[0] == email) {
                            try {
                                // 반송된 이메일 삭제
                                message.setFlag(Flags.Flag.DELETED, true)
                                emailFolder.close(true)
                            } catch (e: MessagingException) {
                                throw ServiceException("500-1", "반송 메일 확인 중 오류가 발생했습니다.")
                            }
                        }
                    }
                    // 폴더가 닫혔다면 반송 메일을 찾았다고 판단
                    if (!emailFolder.isOpen) return false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceException("500", "반송 메일 확인 중 오류가 발생했습니다.")
        }
        return true
    }

    @Throws(MessagingException::class)
    fun getMessageTime(message: Message): LocalDateTime =
        message.sentDate
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

    fun getEmailFolder(): Folder {
        try {
            val properties = Properties().apply {
                put("mail.pop3.host", pop3.host)
                put("mail.pop3.port", pop3.port)
                put("mail.pop3.starttls.enable", "true")
            }
            val emailSession = Session.getDefaultInstance(properties)
            val store: Store = emailSession.getStore(pop3.protocol).apply {
                connect(pop3.host, pop3.username, pop3.password)
            }
            return store.getFolder(pop3.folder).also { it.open(Folder.READ_WRITE) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceException("404-1", "반송 메일을 찾기 못했습니다.")
        }
    }
}
