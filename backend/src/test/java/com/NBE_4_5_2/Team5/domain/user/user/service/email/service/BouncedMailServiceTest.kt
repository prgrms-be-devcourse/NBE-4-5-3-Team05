package com.NBE_4_5_2.Team5.domain.user.user.service.email.service

import com.NBE_4_5_2.Team5.global.config.email.TimeProvider
import com.NBE_4_5_2.Team5.global.config.email.properties.Pop3
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Message
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * BouncedMailServiceTest 클래스는 이메일 반송 처리를 테스트합니다.
 * - 반송 이메일이 존재하면 false를 반환하고, 해당 이메일이 삭제되어 폴더가 닫혀야 합니다.
 * - 반송 이메일이 없으면 true를 반환하며, 폴더는 열린 상태여야 합니다.
 */
@ExtendWith(MockitoExtension::class)
class BouncedMailServiceTest {

    @Mock
    private lateinit var pop3: Pop3                 // Pop3 프로퍼티 모킹

    @Mock
    private lateinit var timeProvider: TimeProvider  // 시간 제공자 모킹

    @InjectMocks
    private lateinit var bouncedEmailService: BouncedEmailService  // 테스트 대상 반송 이메일 서비스

    private lateinit var mockFolder: Folder           // 가짜 폴더
    private lateinit var mockMessage: Message         // 가짜 메시지
    private lateinit var spyService: BouncedEmailService // 스파이 객체 (부분 모킹)

    @BeforeEach
    fun setUp() {
        mockFolder = mock(Folder::class.java)  // Folder 목 객체 생성
        mockMessage = mock(Message::class.java)  // Message 목 객체 생성
        spyService = spy(bouncedEmailService)     // 부분 모킹을 위한 스파이 객체 생성

        doReturn(mockFolder).`when`(spyService).getEmailFolder() // getEmailFolder() 호출 시 가짜 폴더 반환
        `when`(mockFolder.messages).thenReturn(arrayOf(mockMessage))  // 폴더의 메시지 배열 반환 설정
        doReturn(LocalDateTime.now().minusSeconds(10)).`when`(spyService).getMessageTime(mockMessage)  // getMessageTime() 호출 시 현재 시간에서 10초 전 반환
        `when`(pop3.untilTime).thenReturn(120)  // pop3.getUntilTime() 호출 시 120초 반환
    }

    /**
     * 반송된 이메일이 존재하면 false를 반환하는지 테스트합니다.
     */
    @Test
    @DisplayName("email : bounced : 반송된 이메일이 존재하면 false 반환")
    fun test1() {
        val testEmail = "bounced@example.com"  // 테스트용 반송 이메일 주소
        `when`(mockMessage.getHeader("X-Failed-Recipients")).thenReturn(arrayOf(testEmail))
        val result = spyService.checkBouncedEmail(testEmail)  // 반송 이메일 체크 실행
        assertFalse(result)  // 반송된 이메일 존재 시 false 반환 검증
        verify(mockFolder, times(1)).close(true)  // 폴더 닫기 호출 검증
        verify(mockMessage, times(1)).setFlag(Flags.Flag.DELETED, true)  // 메시지 삭제 플래그 설정 검증
    }

    /**
     * 반송된 이메일이 없으면 true를 반환하는지 테스트합니다.
     */
    @Test
    @DisplayName("email : not bounced : 반송된 이메일이 없으면 true 반환")
    fun test2() {
        val testEmail = "valid@example.com"  // 테스트용 유효한 이메일 주소
        `when`(mockMessage.getHeader("X-Failed-Recipients")).thenReturn(null)
        doReturn(true).`when`(mockFolder).isOpen()  // 폴더가 열린 상태로 설정

        val result = spyService.checkBouncedEmail(testEmail)  // 반송 이메일 체크 실행
        assertTrue(result)  // 반송된 이메일이 없으면 true 반환 검증
        verify(mockFolder, never()).close(true)  // 폴더 닫기 호출 없음 검증
    }
}
