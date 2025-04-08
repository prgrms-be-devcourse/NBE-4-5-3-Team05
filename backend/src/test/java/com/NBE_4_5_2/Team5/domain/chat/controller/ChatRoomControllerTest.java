package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChatRoomControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductPostRepository productPostRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    String postId;
    String sender;
    String receiver;
    String token;
    User loginedUser;
    String roomId;
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
        roomId = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data.roomId");
        System.out.println("sender: " + sender);
        System.out.println("roomId: "+roomId);
        return roomId;
    }

//    @AfterEach
    @DisplayName("초기화_ 채팅방 전체 비우기")
    void deleteAll() throws Exception {
        List<ChatRoom> chatRoomList = chatRoomService.findRoomByUser(sender);

        for(ChatRoom chatRoom : chatRoomList){
            roomId = chatRoom.getRoomId();
            mvc.perform(delete("/api/chat/message")
                            .param("roomId", roomId) // 삭제할 채팅방 ID
                            .header("Authorization", "Bearer " + token)
                            .contentType(APPLICATION_JSON))
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("채팅방 생성")
    void createRoom() throws Exception {
        //Given
        deleteAll();    // 채팅방 비우기

        // When: 채팅방 생성 요청
        ResultActions action = mvc.perform(post("/api/chat/room")
                        .param("postId", postId) // 쿼리 파라미터
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print()); // 요청의 Content-Type

        // Then
        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value(containsString(receiver)))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.sender").value(sender))
                .andExpect(jsonPath("$.data.receiver").value(receiver));

        System.out.println("sender: " + sender);
        System.out.println("receiver: " + receiver);
        roomId = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data.roomId");
        System.out.println("roomId: " + roomId);
        System.out.println("채팅방 생성테스트 완료");
    }

    @Test
    @DisplayName("채팅방 조회")
    void getChatRooms() throws Exception {
        //Given: 채팅방 생성
//        ResultActions createAction = mvc.perform(post("/api/chat/room")
//                .param("postId", postId)
//                .header("Authorization", "Bearer " + token)
//                .contentType(APPLICATION_JSON));
//        createAction.andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value("200"));

        // When: 채팅방 조회 요청
        ResultActions action = mvc.perform(get("/api/chat/rooms")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("채팅방 목록"))
                .andExpect(jsonPath("$.data").isNotEmpty()) // 데이터가 비어 있지 않음을 검증
                .andExpect(jsonPath("$.data[0].other").value(receiver));

        roomId = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data[0].roomId");
        String other = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data[0].other");
        System.out.println("roomId: " + roomId);
        System.out.println("other: " + other);
        System.out.println("receiver: " + receiver);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 조회")
    void getNotExitChatRoom() throws Exception {
        // Given
        deleteAll();

        // When: 채팅방 조회 요청
        ResultActions action = mvc.perform(get("/api/chat/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        action.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 채팅방"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("채팅방 삭제")
    void deleteRoom() throws Exception {
        // Given
        List<ChatRoom> chatRoomList = chatRoomService.findRoomByUser(sender);
        roomId = chatRoomList.get(0).getRoomId();

        // When
        ResultActions action = mvc.perform(delete("/api/chat/message")
                        .param("roomId", roomId) // 삭제할 채팅방 ID
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        ResultActions getAction = mvc.perform(get("/api/chat/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());


        // Then
        // 삭제 검증
        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("삭제 완료"))
                .andExpect(jsonPath("$.data").isEmpty());

        // 조회 검증
        getAction.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 채팅방"))
                .andExpect(jsonPath("$.data").isEmpty()); // 데이터가 비어 있음을 검증

        // roomId가 존재하지 않음을 검증
        List<String> roomIds=JsonPath.read(getAction.andReturn().getResponse().getContentAsString(), "$.data[*].roomId");
        assertThat(roomIds).doesNotContain(roomId);
    }

    @Test
    @DisplayName("특정 사용자와의 채팅방 검색")
    void findChatRooms() throws Exception {
        // Given
        createRoom();
        List<ChatRoom> chatRoomList = chatRoomService.findRoomByUser(sender);
        roomId = chatRoomList.get(0).getRoomId();
        receiver = chatRoomList.get(0).getReceiver();
        System.out.println("receiver1: " + receiver);

        // When: 검색 요청
        ResultActions action = mvc.perform(get("/api/chat/search")
                        .param("receiver", receiver)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then: 검색 요청 결과 검증
        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.roomId").value(roomId)); // 정확한 roomId를 반환하는지 검증
        System.out.println("receiver: " + receiver);
        System.out.println("roomId: " + roomId);
        deleteAll();    // 초기화
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 검색")
    void findNotExistChatRoom() throws Exception {
        // Given
        createRoom();
        List<ChatRoom> chatRoomList = chatRoomService.findRoomByUser(sender);
        roomId = chatRoomList.get(0).getRoomId();
        receiver = chatRoomList.get(0).getReceiver();
        System.out.println("roomId: " + roomId);
        System.out.println("receiver: " + receiver);
        deleteAll();    // 채팅방 삭제

        // When: 검색 요청
        ResultActions action = mvc.perform(get("/api/chat/search")
                        .param("receiver", receiver)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then: 검색 요청 결과 검증
        action.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 채팅방"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("roomId로 채팅방 검색")
    void getRoomByRoomId() throws Exception {
        // Given
        roomId = setUpChatRoom();
        System.out.println("검색전, roomId: " + roomId);
        // When
        ResultActions action = mvc.perform(get("/api/chat/room/"+roomId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("채팅방 반환"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.roomId").value(roomId));
    }

    @Test
    @DisplayName("roomId로 채팅방 검색_실패")
    void getNotExistRoomByRoomId() throws Exception {
        // Given
        roomId = setUpChatRoom();
        deleteAll();
        // When
        ResultActions action = mvc.perform(get("/api/chat/room/"+roomId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        action.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 채팅방"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("채팅방 메세지 조회")
    void getMessages() throws Exception {
        // Given
        roomId = setUpChatRoom();

        // When
        ResultActions action = mvc.perform(get("/api/chat/message")
                        .param("roomId", roomId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value(receiver+"와의 대화방"));
                // todo: 메세지까지 조회
    }



    @Test
    @DisplayName("접근 권한 없는 채팅방 메세지 조회")
    void CantAccessGetMessages() throws Exception {
        // Given
        roomId = setUpChatRoom();    // 채팅방 생성
        // 새로운 계정으로 로그인
        User loginedUser2 = userService.getUserByUsername("user2").orElseThrow(
                () -> new RuntimeException("User not found")
        );
        String token2 = userService.generateAuthTokenAsString(loginedUser2);
        System.out.println("토큰: "+ token2);

        // When
        ResultActions action = mvc.perform(get("/api/chat/message")
                        .param("roomId", roomId)
                        .header("Authorization", "Bearer " + token2)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        action.andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("405"))
                .andExpect(jsonPath("$.message").value("접근 권한 없는 채팅방"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("고객센터 연결")
    void createCustomerService() throws Exception {
        // Given
        deleteAll();
        // When
        ResultActions action = mvc.perform(post("/api/chat/admin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());
        // 채팅방 조회
        ResultActions getAction = mvc.perform(get("/api/chat/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON))
                .andDo(print());

        // Then
        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("고객센터"))
                .andExpect(jsonPath("$.data").isNotEmpty());
        // 조회 검증
        getAction.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("채팅방 목록"))
                .andExpect(jsonPath("$.data").isNotEmpty());

        // 관리자 검증
        String receiver = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data.receiver");
        User admin = userService.getUserByUsername(receiver).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        assertEquals(Role.ADMIN, admin.getRole());
    }
    // Given


    // When


    // Then

//
//
//
//
}