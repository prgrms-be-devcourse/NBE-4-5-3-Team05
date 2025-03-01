package com.NBE_4_5_2.Team5.domain.member.controller;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.member.repository.MemberRepository;
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
    private MemberRepository memberRepository;

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

        Member member = memberRepository.findByUsername(username).get();
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

    @Test
    @DisplayName("회원 가입2 - username 중복")
    void signUp2() throws Exception {

        String username = "user1";
        String password = "1234";
        String email = "new@naver.com";
        String nickname = "무명";
        String address = "서울시 강남구";
        String profileUrl = "https://example.com/default_profile.png";

        ResultActions resultActions = signUpRequest(username, password, email, nickname, address, profileUrl);

        resultActions
                .andExpect(status().isConflict())
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signUp"))
                .andExpect(jsonPath("$.code").value("409-1"))
                .andExpect(jsonPath("$.message").value("이미 사용중인 아이디입니다."));

    }

    @Test
    @DisplayName("회원 가입3 - email 중복")
    void signUp3() throws Exception {

        String username = "user4";
        String password = "1234";
        String email = "user1@gmail.com";
        String nickname = "무명";
        String address = "서울시 강남구";
        String profileUrl = "https://example.com/default_profile.png";

        ResultActions resultActions = signUpRequest(username, password, email, nickname, address, profileUrl);

        resultActions
                .andExpect(status().isConflict())
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signUp"))
                .andExpect(jsonPath("$.code").value("409-2"))
                .andExpect(jsonPath("$.message").value("이미 사용중인 이메일입니다."));

    }

    @Test
    @DisplayName("회원 가입4 - nickname 중복")
    void signUp4() throws Exception {

        String username = "user4";
        String password = "1234";
        String email = "user4@gmail.com";
        String nickname = "user1";
        String address = "서울시 강남구";
        String profileUrl = "https://example.com/default_profile.png";

        ResultActions resultActions = signUpRequest(username, password, email, nickname, address, profileUrl);

        resultActions
                .andExpect(status().isConflict())
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signUp"))
                .andExpect(jsonPath("$.code").value("409-3"))
                .andExpect(jsonPath("$.message").value("이미 사용중인 닉네임입니다."));

    }

}