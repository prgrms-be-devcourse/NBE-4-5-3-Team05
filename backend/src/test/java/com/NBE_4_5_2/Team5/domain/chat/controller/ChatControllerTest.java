package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
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
import org.junit.jupiter.api.*;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        setUpUserAndPost();  // 기존 메소드 호출
        String roomId = setUpChatRoom();  // 기존 메소드 호출
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
//        String refreshToken = param[0];
        accessToken = param[1];
        System.out.println("accessToken: " + accessToken);
//        System.out.println("refreshToken: " + refreshToken);
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
                        .param("postId", postId) // 쿼리 파라미터
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print()); // 요청의 Content-Type
        String roomId = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data.roomId");
        System.out.println("sender: " + sender);
        System.out.println("임시 roomId: "+roomId);
        return roomId;
    }

//    @AfterEach
//    @DisplayName("초기화_ 채팅방 전체 비우기")
//    void deleteAll() throws Exception {
//        List<ChatRoom> chatRoomList = chatRoomService.findRoomByUser(sender);
//
//        for(ChatRoom chatRoom : chatRoomList){
//            String roomId = chatRoom.getRoomId();
//            mvc.perform(delete("/api/chat/message")
//                            .param("roomId", roomId) // 삭제할 채팅방 ID
//                            .header("Authorization", "Bearer " + token)
//                            .contentType(APPLICATION_JSON))
//                    .andDo(print());
//        }
//    }

    @Test
    @DisplayName("웹소켓 연결")
    void webSocketConnection() throws Exception {
        // 웹소켓 연결 URL 설정 (WebSocketConfig의 엔드포인트 참고)
        String url = String.format("ws://localhost:%d/ws-stomp", port);

        // WebSocketStompClient 인스턴스 생성 (SockJS 사용)
        stompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));

        // 메시지 컨버터 설정
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        stompClient.setMessageConverter(messageConverter);

        // 웹소켓 연결을 위한 StompHeaders 및 WebSocketHttpHeaders 설정
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("accessToken", accessToken);
        WebSocketHttpHeaders webSocketHeaders = new WebSocketHttpHeaders();

        // 웹소켓 연결 (비동기 방식)
        stompSession = stompClient.connectAsync(url, webSocketHeaders, stompHeaders, new StompSessionHandlerAdapter() {})
                .get(2, TimeUnit.SECONDS); // 최대 2초 대기

        // 연결 성공 여부 확인
        assertNotNull(stompSession, "WebSocket 연결이 실패했습니다.");
        assertTrue(stompSession.isConnected(), "WebSocket 연결이 활성화되지 않았습니다.");

        // 연결 상태 로그 출력
        System.out.println("WebSocket 연결 상태: " + stompSession.isConnected());
//
//        // 테스트 종료 시 웹소켓 연결 종료
//        if (stompSession != null && stompSession.isConnected()) {
//            stompSession.disconnect();
//            System.out.println("WebSocket 연결이 종료되었습니다.");
//        }
    }

    @Test
    @DisplayName("웹소켓 연결 끊기")
    void webSocketDisConnection() throws Exception {
        // given : 연결
//        webSocketConnection();
        assertNotNull(stompSession,"given: 웹소켓이 연결되지 않음");
        assertTrue(stompSession.isConnected(), "given: 웹소켓이 연결되지 않음");

        // when
        stompSession.disconnect();
        System.out.println("종료");
        //then
        assertFalse(stompSession.isConnected(), "WebSocket 연결이 끊기지 않았습니다.");
        System.out.println("연결상태: "+stompSession.isConnected());
    }

    @Test
    @DisplayName("채팅방 구독 요청")
    void SubscribeToChatRoom() throws Exception {
        // 웹소켓 연결이 되어 있는지 확인
        webSocketConnection();
        assertNotNull(stompSession, "WebSocket 연결이 되어 있지 않습니다.");
        assertTrue(stompSession.isConnected(), "WebSocket 연결이 활성화되지 않았습니다.");

        // 채팅방 구독 요청을 위한 destination 설정
        String roomId = setUpChatRoom(); // 채팅방 ID 가져오기
        System.out.println("roomId: "+roomId);
        String destination = "/sub/chat/room/" + roomId;
        System.out.println("destination: " + destination);

        // 구독 요청
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

        // 구독이 성공적으로 이루어졌는지 확인
        System.out.println("채팅방 구독 요청이 완료되었습니다. Destination: " + destination);
        webSocketDisConnection();
    }

}