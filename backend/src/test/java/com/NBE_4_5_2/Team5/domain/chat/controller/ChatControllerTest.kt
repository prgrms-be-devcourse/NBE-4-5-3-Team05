package com.NBE_4_5_2.Team5.domain.chat.controller

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage
import com.NBE_4_5_2.Team5.domain.chat.repository.ChatMessageRepository
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ChatControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var productPostRepository: ProductPostRepository

    @Autowired
    private lateinit var chatRoomService: ChatRoomService

    @Autowired
    private lateinit var chatMessageRepository: ChatMessageRepository

    private lateinit var postId: String
    private lateinit var sender: String
    private lateinit var receiver: String
    private lateinit var token: String
    private lateinit var accessToken: String
    private lateinit var loginedUser: User
    private lateinit var roomId: String
    private var userCount: Long = 0

    private lateinit var stompClient: WebSocketStompClient
    private lateinit var stompSession: StompSession

    @LocalServerPort
    private val port = 0

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        setUpUserAndPost()
        roomId = setUpChatRoom()
    }

    @DisplayName("셋업_ 유저1")
    fun setUpUser() {
        // 로그인 유저 설정
        loginedUser = userService.getUserByUsername("user1").orElseThrow {
            RuntimeException(
                "User not found"
            )
        }
        sender = loginedUser.nickname
        token = userService.generateAuthTokenAsString(loginedUser)
        val param = token.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        accessToken = param[1]
        println("accessToken: $accessToken")
        println("토큰1: $token")
    }

    @DisplayName("셋업_ 게시글,유저")
    fun setUpUserAndPost() {
        // 로그인 유저 설정
        loginedUser = userService.getUserByUsername("user3").orElseThrow {
            RuntimeException(
                "User not found"
            )
        }
        sender = loginedUser.nickname
        token = userService.generateAuthTokenAsString(loginedUser)
        val param = token.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        accessToken = param[1]
        println("accessToken: $accessToken")
        println("토큰1: $token")

        // 포스트 ID 설정
        val post = productPostRepository.findAll().stream()
            .findFirst()
            .orElseThrow { RuntimeException("No posts found") }
        postId = post.id
        receiver = post.writer.username
    }

    @DisplayName("셋업_ 채팅방")
    @Throws(Exception::class)
    fun setUpChatRoom(): String {
        val action = mvc.perform(
            MockMvcRequestBuilders.post("/api/chat/room")
                .param("postId", postId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print()) // 요청의 Content-Type
        val roomId = JsonPath.read<String>(action.andReturn().response.contentAsString, "$.data.roomId")
        println("sender: $sender")
        println("임시 roomId: $roomId")
        return roomId
    }

    @DisplayName("셋업 _ 연결")
    @Throws(Exception::class)
    fun setUp_Connect() {
        val url = String.format("ws://localhost:%d/ws-stomp", port)

        // WebSocketStompClient 인스턴스 생성
        stompClient =
            WebSocketStompClient(SockJsClient(java.util.List.of<Transport>(WebSocketTransport(StandardWebSocketClient()))))

        // 메시지 컨버터 설정
        val messageConverter = MappingJackson2MessageConverter()
        val objectMapper = messageConverter.objectMapper
        objectMapper.registerModules(JavaTimeModule(), ParameterNamesModule())
        stompClient.messageConverter = messageConverter

        // StompHeaders 및 WebSocketHttpHeaders 설정
        val stompHeaders = StompHeaders()
        stompHeaders.add("accessToken", accessToken)
        val webSocketHeaders = WebSocketHttpHeaders()

        // When
        // 웹소켓 연결 (비동기 방식)
        stompSession = stompClient.connectAsync(
            url,
            webSocketHeaders,
            stompHeaders,
            object : StompSessionHandlerAdapter() {})[2, TimeUnit.SECONDS]
    }

    @DisplayName("셋업 _ 구독")
    @Throws(Exception::class)
    fun setUp_Subscribe() {
        // Given
        setUp_Connect()
        Assertions.assertNotNull(stompSession, "WebSocket 연결이 되어 있지 않습니다.")
        Assertions.assertTrue(stompSession.isConnected, "WebSocket 연결이 활성화되지 않았습니다.")
        val destination = "/sub/chat/room/$roomId"

        // When
        stompSession.subscribe(destination, object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders): Type {
                return ChatMessage::class.java
            }

            override fun handleFrame(headers: StompHeaders, payload: Any?) {
                val message = payload as ChatMessage?
                println("Received message: " + message!!.getMessage())
            }
        })

        // Then
        Thread.sleep(100) // 비동기 처리 대기
        userCount = chatRoomService.getUserCount(roomId)
        Assertions.assertEquals(
            1, userCount,
            "구독 실패\nDestination: $destination\nuserCount: $userCount"
        )
        println("채팅방 구독 요청이 완료되었습니다. Destination: $destination")
    }

    @DisplayName("셋업 _ 연결해제")
    @Throws(Exception::class)
    fun setUp_DisConnect() {
        stompSession!!.disconnect()
        println("종료")
    }

    @DisplayName("셋업 - 메세지 전송")
    @Throws(Exception::class)
    fun setUp_SendMessage(content: String, roomId: String) {
        // Given
        // 구독
        setUp_Subscribe()

        val destination = "/pub/chat/message"
        val message = ChatMessage()
        message.setType(ChatMessage.MessageType.TALK)
        message.setMessage(content)
        message.setRoomId(roomId)
        message.setSender(sender)

        // When
        val headers = StompHeaders()
        headers.destination = destination
        headers.add("token", accessToken)
        stompSession.send(headers, message)
    }

    @DisplayName("셋업 _채팅방 삭제")
    @Throws(Exception::class)
    fun setUp_DeleteRoom() {
        // When
        mvc.perform(
            MockMvcRequestBuilders.delete("/api/chat/message")
                .param("roomId", roomId) // 삭제할 채팅방 ID
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
    }

    //
    //
    //
    @Test
    @DisplayName("웹소켓 연결")
    @Throws(Exception::class)
    fun webSocketConnection() {
        // Given
        if (::stompSession.isInitialized && stompSession.isConnected) {
            println("연결해제")
            setUp_DisConnect()
        }
        // URL
        val url = String.format("ws://localhost:%d/ws-stomp", port)

        // WebSocketStompClient 인스턴스 생성
        stompClient =
            WebSocketStompClient(SockJsClient(java.util.List.of<Transport>(WebSocketTransport(StandardWebSocketClient()))))

        // 메시지 컨버터 설정
        val messageConverter = MappingJackson2MessageConverter()
        val objectMapper = messageConverter.objectMapper
        objectMapper.registerModules(JavaTimeModule(), ParameterNamesModule())
        stompClient.messageConverter = messageConverter

        // StompHeaders 및 WebSocketHttpHeaders 설정
        val stompHeaders = StompHeaders()
        stompHeaders.add("accessToken", accessToken)
        val webSocketHeaders = WebSocketHttpHeaders()

        // When
        // 웹소켓 연결 (비동기 방식)
        stompSession = stompClient.connectAsync(
            url,
            webSocketHeaders,
            stompHeaders,
            object : StompSessionHandlerAdapter() {})[2, TimeUnit.SECONDS]

        // Then
        Assertions.assertNotNull(stompSession, "WebSocket 연결이 실패했습니다.")
        Assertions.assertTrue(stompSession.isConnected(), "WebSocket 연결이 활성화되지 않았습니다.")
        println("WebSocket 연결 상태: " + stompSession.isConnected())
        // 연결 해제
        setUp_DisConnect()
    }

    @Test
    @DisplayName("연결 끊기")
    @Throws(Exception::class)
    fun webSocketDisConnection() {
        // Given
        // 구독
        setUp_Subscribe()

        Assertions.assertNotNull(stompSession, "Given: 웹소켓이 연결되지 않음")
        Assertions.assertTrue(stompSession.isConnected, "Given: 웹소켓이 연결되지 않음")
        Assertions.assertEquals(1, userCount, "Given: 구독 실패")
        println("종료 전, userCount: $userCount")

        // When
        stompSession.disconnect()
        println("종료")
        Thread.sleep(100) // 비동기 처리 대기

        // Then
        Assertions.assertFalse(stompSession.isConnected, "WebSocket 연결이 끊기지 않았습니다.")
        userCount = chatRoomService.getUserCount(roomId)
        Assertions.assertEquals(0, userCount, "채팅방 인원수가 감소되지 않았습니다.")
        println("종료 후, userCount: $userCount")

        // 세션 정보 삭제 확인
        val sessionId = stompSession.sessionId
        Assertions.assertNull(chatRoomService.getUserEnterRoomId(sessionId), "세션 정보가 삭제되지 않았습니다.")

        // 로그 출력
        println("STOMP DISCONNECT 테스트 완료. 세션 ID: $sessionId")
    }

    @Test
    @DisplayName("채팅방 구독 요청")
    @Throws(Exception::class)
    fun subscribeToChatRoom() {
        // Given
        // 연결
        setUp_Connect()
        Assertions.assertNotNull(stompSession, "WebSocket 연결이 되어 있지 않습니다.")
        Assertions.assertTrue(stompSession.isConnected, "WebSocket 연결이 활성화되지 않았습니다.")
        val destination = "/sub/chat/room/$roomId"

        // When
        stompSession!!.subscribe(destination, object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders): Type {
                return ChatMessage::class.java
            }

            override fun handleFrame(headers: StompHeaders, payload: Any?) {
                val message = payload as ChatMessage?
                println("Received message: " + message!!.getMessage())
            }
        })

        // Then
        Thread.sleep(100) // 비동기 처리 대기
        userCount = chatRoomService.getUserCount(roomId)
        Assertions.assertEquals(
            1, userCount,
            "구독 실패\nDestination: $destination\nuserCount: $userCount"
        )
        println("채팅방 구독 요청이 완료되었습니다. Destination: $destination")
        // 연결 해제
        setUp_DisConnect()
    }

    @ParameterizedTest
    @ValueSource(strings = ["메세지 1", "메세지 2", "메세지 3"])
    @DisplayName("메세지 전송")
    @Throws(
        Exception::class
    )
    fun sendMessage(content: String) {
        // Given
        // 구독
        setUp_Subscribe()

        val destination = "/pub/chat/message"
        val message = ChatMessage()
        message.setType(ChatMessage.MessageType.TALK)
        message.setMessage(content)
        message.setRoomId(roomId)
        message.setSender(sender)

        // When
        val headers = StompHeaders()
        headers.destination = destination
        headers.add("token", accessToken)

        stompSession.send(headers, message)

        // Then
        Thread.sleep(500) // 비동기 처리

        val messages = chatRoomService.getMessagesByUser(roomId, sender)
        Assertions.assertFalse(messages.isEmpty(), "메세지가 전송되지 않았습니다.")
        val lastMessage = messages[messages.size - 1]
        Assertions.assertEquals(content, lastMessage.getMessage(), "메세지 내용이 일치하지 않습니다.")
        println("messages: " + lastMessage.getMessage())

        setUp_DeleteRoom() // 채팅방 비우기
        setUp_DisConnect() // 연결 해제
    }

    @Throws(Exception::class)
    @Test
    @DisplayName("채팅방 메세지 조회")
    fun testGetMessages() {
        // Given
        val content = "테스트 메세지1"
        roomId = setUpChatRoom()
        setUp_SendMessage(content, roomId!!)
        Thread.sleep(100)

        // When
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/message")
                .param("roomId", roomId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // Then
        val result = action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${receiver}와의 대화방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].message").value(Matchers.hasItem(content)))
            .andReturn()

        val responseContent = result.response.contentAsString
        val messages = JsonPath.read<List<String>>(responseContent, "$.data[*].message")
        val data = JsonPath.read<List<String>>(responseContent, "$.data[*]")
        println("메세지 리스트: $messages")
        println("================== data ==================\n$data")
        setUp_DisConnect() // 연결 해제
        setUp_DeleteRoom() // 채팅방 비우기
    }

    @Test
    @DisplayName("채팅방 메세지 삭제")
    @Throws(Exception::class)
    fun deleteMessage() {
        // Given
        val content = "삭제할 메세지"
        roomId = setUpChatRoom()
        setUp_SendMessage(content, roomId)
        Thread.sleep(100)

        var action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/message")
                .param("roomId", roomId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        var result = action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${receiver}와의 대화방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].message").value(Matchers.hasItem(content)))
            .andReturn()

        var responseContent = result.response.contentAsString
        var data = JsonPath.read<List<String>>(responseContent, "$.data[*]")
        println("================== 삭제 전 ==================\n$data")

        // When
        // 삭제
        setUp_DeleteRoom()
        action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/message")
                .param("roomId", roomId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        result = action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${receiver}와의 대화방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
            .andReturn()

        responseContent = result.response.contentAsString
        data = JsonPath.read<List<String>>(responseContent, "$.data[*]")
        println("================== 삭제 후 원래 계정 ==================\n$data")

        // 반대쪽 계정 로그인
        receiver = sender
        setUpUser()

        action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/message")
                .param("roomId", roomId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // Then
        result = action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${receiver}와의 대화방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[*].message").value(Matchers.hasItem(content)))
            .andReturn()

        responseContent = result.response.contentAsString
        data = JsonPath.read<List<String>>(responseContent, "$.data[*]")
        println("================== 새로운 계정 ==================\n$data")
        setUp_DisConnect() // 연결 해제
        setUp_DeleteRoom() // 채팅방 비우기
    }
}