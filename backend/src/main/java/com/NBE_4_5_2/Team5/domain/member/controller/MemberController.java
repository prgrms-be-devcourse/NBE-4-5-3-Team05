package com.NBE_4_5_2.Team5.domain.member.controller;

import com.NBE_4_5_2.Team5.domain.member.dto.MemberDto;
import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.member.service.MemberService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    record JoinMemberForm(String username, String password, String email,
                          String nickname, String address, String profileUrl) {
    }

    @PostMapping("/signup")
    public RsData<MemberDto> signUp(@RequestBody @Valid JoinMemberForm memberForm) {

        Member member = memberService.signUp(memberForm.username, memberForm.password, memberForm.email,
                memberForm.nickname, memberForm.address, memberForm.profileUrl);

        return new RsData<>(
                "201-1",
                "회원 가입이 완료되었습니다.",
                new MemberDto(member)
        );
    }

}
