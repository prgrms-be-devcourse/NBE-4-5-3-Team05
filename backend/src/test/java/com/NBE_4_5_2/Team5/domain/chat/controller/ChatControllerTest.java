package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.repository.ChatMessageRepository;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductPostRepository productPostRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    String postId;
    String sender;
    String receiver;
    String token;
    String accessToken;
    User loginedUser;
    String roomId;
    long userCount;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        setUpUserAndPost();
        roomId = setUpChatRoom();
    }

    @DisplayName("셋업_ 게시글,유저")
    void setUpUserAndPost() {
        // 로그인 유저 설정
        loginedUser = userService.getUserByUsername("user3").orElseThrow(
                () -> new RuntimeException("User not found")
        );
        sender = loginedUser.getNickname();
        token = userService.generateAuthTokenAsString(loginedUser);
        String[] param = token.split(" ");
        accessToken = param[1];
        System.out.println("accessToken: " + accessToken);
        System.out.println("토큰1: "+token);

        // 포스트 ID 설정
        ProductPost post = productPostRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No posts found"));
        postId = post.getId();
        receiver = post.getWriter().getUsername();
    }

    @DisplayName("셋업_ 채팅방")
    String setUpChatRoom() throws Exception {
        ResultActions action = mvc.perform(post("/api/chat/room")
                        .param("postId", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print()); // 요청의 Content-Type
        String roomId = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data.roomId");
        System.out.println("sender: " + sender);
        System.out.println("임시 roomId: "+roomId);
        return roomId;
    }

    @DisplayName("셋업 _ 연결")
    void setUp_Connect() throws Exception{
        String url = String.format("ws://localhost:%d/ws-stomp", port);

        // WebSocketStompClient 인스턴스 생성
        stompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));

        // 메시지 컨버터 설정
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        stompClient.setMessageConverter(messageConverter);

        // StompHeaders 및 WebSocketHttpHeaders 설정
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("accessToken", accessToken);
        WebSocketHttpHeaders webSocketHeaders = new WebSocketHttpHeaders();

        // When
        // 웹소켓 연결 (비동기 방식)
        stompSession = stompClient.connectAsync(url, webSocketHeaders, stompHeaders, new StompSessionHandlerAdapter() {})
                .get(2, TimeUnit.SECONDS);
    }

    @DisplayName("셋업 _ 구독")
    void setUp_Subscribe() throws Exception{
        // Given
        setUp_Connect();
        assertNotNull(stompSession, "WebSocket 연결이 되어 있지 않습니다.");
        assertTrue(stompSession.isConnected(), "WebSocket 연결이 활성화되지 않았습니다.");
        String destination = "/sub/chat/room/" + roomId;

        // When
        stompSession.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                ChatMessage message = (ChatMessage) payload;
                System.out.println("Received message: " + message.getMessage());
            }
        });

        // Then
        Thread.sleep(50); // 비동기 처리 대기
        userCount = chatRoomService.getUserCount(roomId);
        assertEquals(1, userCount,"구독 실패\nDestination: "+destination + "\nuserCount: " + userCount);
        System.out.println("채팅방 구독 요청이 완료되었습니다. Destination: " + destination);
    }

    @DisplayName("셋업 _ 연결해제")
    void setUp_DisConnect() throws Exception{
        stompSession.disconnect();
        System.out.println("종료");
//        Thread.sleep(50); // 비동기 처리 대기
    }

    @DisplayName("셋업 - 메세지 전송")
    void setUp_SendMessage(String content) throws Exception {
        // Given
        // 구독
        setUp_Subscribe();

        String destination = "/pub/chat/message";
        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.TALK);
        message.setMessage(content);
        message.setRoomId(roomId);
        message.setSender(sender);

        // When
        StompHeaders headers = new StompHeaders();
        headers.setDestination(destination);
        headers.add("token", accessToken);
        stompSession.send(headers, message);
    }

    @DisplayName("셋업 _채팅방 삭제")
    void setUp_DeleteRoom() throws Exception {
        // When
        mvc.perform(delete("/api/chat/message")
                        .param("roomId", roomId) // 삭제할 채팅방 ID
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());
    }

    //
    //
    //

    @Test
    @DisplayName("웹소켓 연결")
    void webSocketConnection() throws Exception {
        // Given
        if(stompSession != null && stompSession.isConnected()) {
            System.out.println("연결해제");
            setUp_DisConnect();
        }
        // URL
        String url = String.format("ws://localhost:%d/ws-stomp", port);

        // WebSocketStompClient 인스턴스 생성
        stompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));

        // 메시지 컨버터 설정
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        stompClient.setMessageConverter(messageConverter);

        // StompHeaders 및 WebSocketHttpHeaders 설정
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("accessToken", accessToken);
        WebSocketHttpHeaders webSocketHeaders = new WebSocketHttpHeaders();

        // When
        // 웹소켓 연결 (비동기 방식)
        stompSession = stompClient.connectAsync(url, webSocketHeaders, stompHeaders, new StompSessionHandlerAdapter() {})
                .get(2, TimeUnit.SECONDS);

        // Then
        assertNotNull(stompSession, "WebSocket 연결이 실패했습니다.");
        assertTrue(stompSession.isConnected(), "WebSocket 연결이 활성화되지 않았습니다.");
        System.out.println("WebSocket 연결 상태: " + stompSession.isConnected());
        // 연결 해제
        setUp_DisConnect();
    }

    @Test
    @DisplayName("연결 끊기")
    void webSocketDisConnection() throws Exception {
        // Given
        // 구독
        setUp_Subscribe();

        assertNotNull(stompSession,"Given: 웹소켓이 연결되지 않음");
        assertTrue(stompSession.isConnected(), "Given: 웹소켓이 연결되지 않음");
        assertEquals(1, userCount,"Given: 구독 실패");
        System.out.println("종료 전, userCount: "+userCount);

        // When
        stompSession.disconnect();
        System.out.println("종료");
        Thread.sleep(50); // 비동기 처리 대기

        // Then
        assertFalse(stompSession.isConnected(), "WebSocket 연결이 끊기지 않았습니다.");
        userCount = chatRoomService.getUserCount(roomId);
        assertEquals(0, userCount, "채팅방 인원수가 감소되지 않았습니다.");
        System.out.println("종료 후, userCount: "+userCount);

        // 세션 정보 삭제 확인
        String sessionId = stompSession.getSessionId();
        assertNull(chatRoomService.getUserEnterRoomId(sessionId), "세션 정보가 삭제되지 않았습니다.");

        // 로그 출력
        System.out.println("STOMP DISCONNECT 테스트 완료. 세션 ID: " + sessionId);
    }

    @Test
    @DisplayName("채팅방 구독 요청")
    void subscribeToChatRoom() throws Exception {
        // Given
        // 연결
        setUp_Connect();
        assertNotNull(stompSession, "WebSocket 연결이 되어 있지 않습니다.");
        assertTrue(stompSession.isConnected(), "WebSocket 연결이 활성화되지 않았습니다.");
        String destination = "/sub/chat/room/" + roomId;

        // When
        stompSession.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                ChatMessage message = (ChatMessage) payload;
                System.out.println("Received message: " + message.getMessage());
            }
        });

        // Then
        Thread.sleep(50); // 비동기 처리 대기
        userCount = chatRoomService.getUserCount(roomId);
        assertEquals(1, userCount,"구독 실패\nDestination: "+destination + "\nuserCount: " + userCount);
        System.out.println("채팅방 구독 요청이 완료되었습니다. Destination: " + destination);
        // 연결 해제
        setUp_DisConnect();
    }

    @ParameterizedTest
    @ValueSource(strings = {"메세지 1", "메세지 2", "메세지 3"})
    @DisplayName("메세지 전송")
    void sendMessage(String content) throws Exception {
        // Given
        // 구독
        setUp_Subscribe();

        String destination = "/pub/chat/message";
        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.TALK);
        message.setMessage(content);
        message.setRoomId(roomId);
        message.setSender(sender);

        // When
        StompHeaders headers = new StompHeaders();
        headers.setDestination(destination);
        headers.add("token", accessToken);

        stompSession.send(headers, message);

        // Then
        Thread.sleep(50); // 비동기 처리
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);
        assertFalse(messages.isEmpty(),"메세지가 전송되지 않았습니다.");
        ChatMessage lastMessage = messages.get(messages.size()-1);
        assertEquals(content, lastMessage.getMessage(),"메세지 내용이 일치하지 않습니다.");
        System.out.println("messages: " + lastMessage.getMessage());

        setUp_DisConnect(); // 연결 해제
        setUp_DeleteRoom(); // 채팅방 비우기
    }

    @Test
    @DisplayName("채팅방 메세지 조회")
    void getMessages() throws Exception {
        // Given
        String content = "테스트 메세지1";
        setUp_SendMessage(content);
        roomId = setUpChatRoom();

        // When
        ResultActions action = mvc.perform(get("/api/chat/message")
                        .param("roomId", roomId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        MvcResult result = action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value(receiver+"와의 대화방"))
                .andExpect(jsonPath("$.data[*].message").value(hasItem(content)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<String> messages = JsonPath.read(responseContent, "$.data[*].message");
        System.out.println("메세지 리스트: "+messages);
    }

}