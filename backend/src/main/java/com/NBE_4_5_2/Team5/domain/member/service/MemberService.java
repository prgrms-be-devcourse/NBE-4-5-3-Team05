package com.NBE_4_5_2.Team5.domain.member.service;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.member.repository.MemberRepository;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member signUp(String username, String password, String email,
                         String nickname, String address, String profileUrl) {

        validateDuplicateMember(username, email, nickname);

        Member member = Member.builder()
                .id("user-" + UUID.randomUUID().toString())
                .username(username)
                .password(password)
                .email(email)
                .nickname(nickname)
                .address(address)
                .profileUrl(profileUrl)
                .build();

        return memberRepository.save(member);
    }

    public void validateDuplicateMember(String username, String email, String nickname) {

        memberRepository.findByUsername(username)
                .ifPresent(member -> {
                    throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                });

        memberRepository.findByEmail(email)
                .ifPresent(member -> {
                    throw new ServiceException("409-2", "이미 사용중인 이메일입니다.");
                });

        memberRepository.findByNickname(nickname)
                .ifPresent(member -> {
                    throw new ServiceException("409-3", "이미 사용중인 닉네임입니다.");
                });

    }

    public long count() {
        return memberRepository.count();
    }

}
