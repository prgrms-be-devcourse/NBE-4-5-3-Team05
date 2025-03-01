package com.NBE_4_5_2.Team5.domain.member.controller;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.member.service.MemberService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    private void checkUser(ResultActions resultActions, Member member) throws Exception {
        resultActions
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.username").value(member.getUsername()))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.address").value(member.getAddress()))
                .andExpect(jsonPath("$.data.profileUrl").value(member.getProfileUrl()))
                .andExpect(jsonPath("$.data.role").value(member.getRole()))
                .andExpect(jsonPath("$.data.createdAt").value(matchesPattern(member.getCreatedAt().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedAt").value(matchesPattern(member.getModifiedAt().toString().replaceAll("0+$", "") + ".*")));
    }

    private ResultActions signUpRequest(String username, String password, String email, String nickname,
                                        String address, String profileUrl) throws Exception {
        return mvc
                .perform(
                        post("/api/users/signup")
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "%s",
                                          "email": "%s",
                                          "nickname": "%s",
                                          "address": "%s",
                                          "profileUrl": "%s"
                                        }
                                        """
                                        .formatted(username, password, email, nickname, address, profileUrl)
                                        .stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입1")
    void signUp1() throws Exception {

        String username = "userNew";
        String password = "1234";
        String email = "new@naver.com";
        String nickname = "무명";
        String address = "서울시 강남구";
        String profileUrl = "default_profile.png";

        ResultActions resultActions = signUpRequest(username, password, email, nickname, address, profileUrl);

        Member member = memberService.findByUsername(username).get();
        assertThat(member.getNickname()).isEqualTo(nickname);
        assertThat(member.getId()).startsWith("user-");

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signUp"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.message").value("회원 가입이 완료되었습니다."));

        checkUser(resultActions, member);

    }


}