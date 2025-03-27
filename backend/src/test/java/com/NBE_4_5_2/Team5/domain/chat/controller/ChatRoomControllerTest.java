package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("채팅방 생성")
    void createRoom() throws Exception {
        //Given
//        System.out.println("sender: " + sender);
//        System.out.println("receiver: " + receiver);
        System.out.println("token: " + token);
        System.out.println("postId: " + postId);

        // When: 채팅방 생성 요청
        ResultActions action = mvc.perform(post("/api/chat/room") // URL 설정
                        .param("postId", postId) // 쿼리 파라미터
                        .header("Authorization", "Bearer " + token) // 토큰 추가
                        .contentType(APPLICATION_JSON)) // 요청의 Content-Type
                .andExpect(status().isOk()); // 상태 코드 검사

        action.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value(containsString(receiver)))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.sender").value(sender))
                .andExpect(jsonPath("$.data.receiver").value(receiver));
        System.out.println("sender: " + sender);
        System.out.println("receiver: " + receiver);
        System.out.println("채팅방 생성테스트 완료");
    }

//    @Test
//    void getUserRooms() {
//    }
//
//    @Test
//    void getRoomByRoomId() {
//    }
//
//    @Test
//    void getMessages() {
//    }
//
//    @Test
//    void deleteRoom() {
//    }
//
//    @Test
//    void findChatRooms() {
//    }
}