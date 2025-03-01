package com.NBE_4_5_2.Team5.domain.member.dto;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberDto {
    private String id;
    private String username;
    private String email;
    private String nickname;
    private String address;
    private String profileUrl;
    private Integer role;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.address = member.getAddress();
        this.profileUrl = member.getProfileUrl();
        this.role = member.getRole();
        this.createdAt = member.getCreatedAt();
        this.modifiedAt = member.getModifiedAt();
    }
}
