package com.NBE_4_5_2.Team5.domain.user.user.service.email.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.global.config.email.Pop3Properties;
import com.NBE_4_5_2.Team5.global.config.email.TimeProvider;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BouncedEmailService {

	private final Pop3Properties pop3Properties;
	private static final String FAILED_RECIPIENTS_HEADER = "X-Failed-Recipients";
	private final TimeProvider timeProvider;

	/**
	 * 발송된 이메일 주소가 존재하는 이메일인지 확인
	 * 메일함으로 반송 메일이 오기까지 대기하기 위해 Thread.sleep 적용
	 *
	 * @param email 발송되었는지 확인할 이메일
	 * @return 메일함에 해당 이메일로 반송된 메일이 존재할 경우 false, 없으면 true
	 * */
	public boolean checkBouncedEmail(String email) {
		try {
			timeProvider.sleep(3000); // 반송 메일이 도착하기까지 대기
			Folder emailFolder = getEmailFolder();
			LocalDateTime untilTime = LocalDateTime.now().minusSeconds(pop3Properties.getUntilTime()); // 현재시간 - 2분

			for (Message message : emailFolder.getMessages()) {
				LocalDateTime messageTime = getMessageTime(message);

				if (untilTime.isBefore(messageTime)) { // untilTime 이후로 온 메일이라면 확인
					Optional.ofNullable(message.getHeader(FAILED_RECIPIENTS_HEADER))
						.ifPresent(recipients -> {

							// 사용자가 요청한 email에 대한 반송 메일이 존재할 경우
							if (recipients[0].equals(email)) {
								try {
									// 반송된 이메일 삭제
									message.setFlag(Flags.Flag.DELETED, true);
									emailFolder.close(true);
								} catch (MessagingException e) {
									throw new ServiceException("500-1", "반송 메일 확인 중 오류가 발생했습니다.");
								}
							}
						});
					// 폴더를 닫았다면 반송 메일을 찾고 삭제했다고 판단하여 false를 반환함
					if (!emailFolder.isOpen()) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			throw new ServiceException("500", "반송 메일 확인 중 오류가 발생했습니다.");
		}
		return true;
	}

	LocalDateTime getMessageTime(Message message) throws MessagingException {
		return message.getSentDate()
			.toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDateTime();
	}

	Folder getEmailFolder() {
		try {
			Properties properties = new Properties();
			properties.put("mail.pop3.host", pop3Properties.getHost());
			properties.put("mail.pop3.port", pop3Properties.getPort());
			properties.put("mail.pop3.starttls.enable", "true");
			Session emailSession = Session.getDefaultInstance(properties);

			Store store = emailSession.getStore(pop3Properties.getProtocol());
			store.connect(pop3Properties.getHost(), pop3Properties.getUsername(), pop3Properties.getPassword());

			Folder emailFolder = store.getFolder(pop3Properties.getFolder());
			emailFolder.open(Folder.READ_WRITE);
			return emailFolder;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("404-1", "반송 메일을 찾기 못했습니다.");
		}
	}

}
