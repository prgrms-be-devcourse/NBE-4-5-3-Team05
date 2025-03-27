package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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

    @BeforeEach
    @DisplayName("셋업")
    void setUp() {
        // 로그인 유저 설정
        loginedUser = userService.getUserByUsername("user3").orElseThrow(
                () -> new RuntimeException("User not found")
        );
        sender = loginedUser.getNickname();
        token = userService.generateAuthTokenAsString(loginedUser);

        // 포스트 ID 설정
        ProductPost post = productPostRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No posts found"));
        postId = post.getId();
        receiver = post.getWriter().getUsername();
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("채팅방 생성")
    void createRoom() throws Exception {
        //Given
//        System.out.println("sender: " + sender);
//        System.out.println("receiver: " + receiver);
//        System.out.println("token: " + token);
//        System.out.println("postId: " + postId);

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
        String roomId = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data.roomId");
        System.out.println("roomId: " + roomId);
        System.out.println("채팅방 생성테스트 완료");
    }

    @Test
    @Transactional // 각 테스트 각각의 세션을 유지
    @Rollback
    @DisplayName("채팅방 조회")
    void getChatRooms() throws Exception {
        //Given
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

        String roomId = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data[0].roomId");
        String other = JsonPath.read(action.andReturn().getResponse().getContentAsString(), "$.data[0].other");
        System.out.println("roomId: " + roomId);
        System.out.println("other: " + other);
        System.out.println("receiver: " + receiver);
    }

    @Test
    @DisplayName("채팅방 삭제")
    void deleteRoom() throws Exception {
        // Given
        List<ChatRoom> chatRoomList = chatRoomService.findRoomByUser(sender);
        String roomId = chatRoomList.get(0).getRoomId();

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
                .andExpect(jsonPath("$.data").isEmpty()); // 데이터가 비어있음을 검증
        // 조회 검증
        getAction.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("채팅방 목록"))
                .andExpect(jsonPath("$.data").isEmpty()); // 데이터가 비어 있음을 검증


    }


//
//    @Test
//    void getRoomByRoomId() {
//    }
//
//    @Test
//    void getMessages() {
//    }
//
//
//    @Test
//    void findChatRooms() {
//    }
}