package com.NBE_4_5_2.Team5.global.security;

import com.NBE_4_5_2.Team5.domain.user.controller.UserController;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomAuthenticationFilterTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private Rq rq;

    private User loginedUser;

    @BeforeEach
    void setUp() {
        loginedUser = userService.getUserByUsername("user1").get();

        // 인증 정보가 초기화된 상태로 테스트 진행
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 - 성공 - 토큰이 없으나 인증 되어있음")
    void test1() throws Exception {

        rq.setLogin(loginedUser);

        ResultActions resultActions = mvc
                .perform(get("/api/users/me"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("getCurrentUser"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.message").value("내 정보 조회가 완료되었습니다."));

    }

    @Test
    @DisplayName("인증 - 실패 - 인증정보가 없고 토큰도 없음")
    void test2() throws Exception {

        ResultActions resultActions = mvc
                .perform(get("/api/users/me")) // 인증이 필요한 경로
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.message").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("인증 - 실패 - 익명 사용자 요청")
    void test3() throws Exception {

        SecurityContextHolder.getContext().setAuthentication( // 익명 사용자로 SecurityContext 설정
                new AnonymousAuthenticationToken(
                        "anonymous", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        ResultActions resultActions = mvc
                .perform(get("/api/users/me"))
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.message").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("인증 - 실패 - 인증되지 않은 사용자 요청")
    void test4() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unknown_user", null, List.of())
        );

        ResultActions resultActions = mvc
                .perform(get("/api/users/me"))
                .andDo(print());

        // rq.getUserIdentity()에서 인증되지 않은 사용자를 예외 처리함
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401-2"))
                .andExpect(jsonPath("$.message").value("잘못된 인증 정보입니다"));
    }

}
