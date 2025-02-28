package com.NBE_4_5_2.Team5.global.response;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import com.NBE_4_5_2.Team5.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    private final MemberRepository memberRepository;

    @Lazy
    @Autowired
    private DevInitData self;

    @Bean
    public ApplicationRunner applicationRunner(){
        return args -> {
            self.memberInit();
        };
    }

    @Transactional
    public void memberInit(){
        if(memberRepository.count() > 0) {
            return;
        }

        memberRepository.save(new Member());
    }

}
