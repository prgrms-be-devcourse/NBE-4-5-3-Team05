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
     * 발송된 이메일 주소가 존재하는 이메일인지 확인
     * 메일함으로 반송 메일이 오기까지 대기하기 위해 Thread.sleep 적용
     *
     * @param email 발송되었는지 확인할 이메일
     * @return 메일함에 해당 이메일로 반송된 메일이 존재할 경우 false, 없으면 true
     */
    fun checkBouncedEmail(email: String): Boolean {
        try {

            // gmail에 반송된 메일이 도착하기까지 3초 대기
            timeProvider.sleep(3000)

            // 현재시간 - 2분 (최근 2분 내에 도착한 메일만 확인하기 위해 사용)
            val untilTime = LocalDateTime.now().minusSeconds(pop3.untilTime.toLong())

            // 메일함의 메일을 하나씩 확인
            for (message in emailFolder.messages) {
                // 해당 메일이 도착한 시간을 확인
                val messageTime = getMessageTime(message)

                // 해당 메일 도착 시간이 2분 이내라면
                if (untilTime.isBefore(messageTime)) {

                    // 반송된 이메일에 대한 수신자 정보를 담은 헤더를 가져옴
                    message.getHeader(FAILED_RECIPIENTS_HEADER)?.let {

                        // 사용자가 요청한 email과 반송된 이메일의 수신자가 같을 경우
                        if (it[0] == email) {
                            try {
                                // 반송된 이메일을 메일함에서 삭제 후 메일함을 닫음
                                message.setFlag(Flags.Flag.DELETED, true)
                                emailFolder.close(true)
                            } catch (e: MessagingException) {
                                throw ServiceException("500-1", "반송 메일 확인 중 오류가 발생했습니다.")
                            }
                        }
                    }
                }
                // 폴더를 닫았다면 반송 메일을 찾고 삭제했다고 판단하여 false를 반환함
                if (!emailFolder.isOpen) return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServiceException("500", "반송 메일 확인 중 오류가 발생했습니다.")
        }
        return true // 반송메일이 존재하지 않는 경우 true 반환
    }

    /**
     * 해당 메세지의 발송 시간을 LocalDateTime으로 변환하여 반환
     */
    @Throws(MessagingException::class)
    fun getMessageTime(message: Message): LocalDateTime =
        message
            .sentDate
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()


    /**
     * 실제 gmail의 메일함을 가져오는 메소드
     * */
    val emailFolder: Folder
        get() {
            try {
                val properties = Properties().apply {
                    put("mail.pop3.host", pop3.host)
                    put("mail.pop3.port", pop3.port)
                    put("mail.pop3.starttls.enable", "true")
                }

                val store = Session.getDefaultInstance(properties)
                    .getStore(pop3.protocol).also{
                        it.connect(pop3.host, pop3.username, pop3.password)
                    }

                return store.getFolder(pop3.folder).also { it.open(Folder.READ_WRITE) }
            } catch (e: Exception) {
                e.printStackTrace()
                throw ServiceException("404-1", "반송 메일을 찾기 못했습니다.")
            }
        }
}
