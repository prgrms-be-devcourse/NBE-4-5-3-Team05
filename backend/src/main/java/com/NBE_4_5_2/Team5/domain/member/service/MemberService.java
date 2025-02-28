package com.NBE_4_5_2.Team5.domain.member.service;

import com.NBE_4_5_2.Team5.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
}
