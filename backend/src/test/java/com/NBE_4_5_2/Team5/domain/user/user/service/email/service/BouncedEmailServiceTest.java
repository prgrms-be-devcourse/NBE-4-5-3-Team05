package com.NBE_4_5_2.Team5.domain.user.user.service.email.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.NBE_4_5_2.Team5.global.config.email.Pop3Properties;
import com.NBE_4_5_2.Team5.global.config.email.TimeProvider;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;

@ExtendWith(MockitoExtension.class)
class BouncedEmailServiceTest  {

	@Mock
	private Pop3Properties pop3Properties;
	@Mock
	private TimeProvider timeProvider;
	@InjectMocks
	private BouncedEmailService bouncedEmailService;

	private Folder mockFolder;
	private Message mockMessage;
	private BouncedEmailService spyService;

	@BeforeEach
	void setUp() throws Exception {
		mockFolder = mock(Folder.class);
		mockMessage = mock(Message.class);
		spyService = spy(bouncedEmailService);

		// 가짜 폴더를 반환하도록 설정
		doReturn(mockFolder).when(spyService).getEmailFolder();

		// 폴더에서 메시지를 가져올 때 가짜 메시지 반환
		when(mockFolder.getMessages()).thenReturn(new Message[] {mockMessage});

		// 메세지의 발송 시간을 검증할 때 항상 현재시간 - 10초 반환
		doReturn(LocalDateTime.now().minusSeconds(10)).when(spyService).getMessageTime(mockMessage);

		// yml에 설정된 인증메일 만료 시간을 2분으로 설정
		when(pop3Properties.getUntilTime()).thenReturn(120);
	}

	@Test
	@DisplayName("email : bounced : 반송된 이메일이 존재하면 false 반환")
	void test1() throws Exception {
		// given
		String testEmail = "bounced@example.com";
		when(mockMessage.getHeader("X-Failed-Recipients")).thenReturn(new String[] {testEmail});

		// when
		boolean result = spyService.checkBouncedEmail(testEmail);

		// then
		assertFalse(result); // 반송된 이메일이 존재하면 false 반환
		verify(mockFolder, times(1)).close(true); // 폴더 닫기 실행 확인
		verify(mockMessage, times(1)).setFlag(Flags.Flag.DELETED, true); // 반송된 이메일 삭제 확인
	}

	@Test
	@DisplayName("email : not bounced : 반송된 이메일이 없으면 true 반환")
	void test2() throws Exception {
		// given
		String testEmail = "valid@example.com";

		when(mockMessage.getHeader("X-Failed-Recipients")).thenReturn(null); // 반송 메일이 없도록 설정
		doReturn(true).when(mockFolder).isOpen();

		// when
		boolean result = spyService.checkBouncedEmail(testEmail);

		// then
		assertTrue(result); // 반송된 이메일이 없으면 true 반환
		verify(mockFolder, never()).close(true); // 폴더가 닫히지 않아야 함
	}
}
