package com.NBE_4_5_2.Team5.domain.member.entity;

import com.NBE_4_5_2.Team5.global.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Member extends BaseTime {

    @Column(length = 20, nullable = false, unique = true)
    private String username;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 255)
    private String address;

    @Column(name = "profile_url", length = 255)
    private String profileUrl;

    @Column(nullable = false)
    private Integer role;  // 0: Admin, 1: 일반 유저

    @Column(nullable = false)
    private Boolean blocked = false; // 계정이 잠겼는지 여부

    @Column(name = "blocked_count", nullable = false)
    private Integer blockedCount = 0; // 계정 차단 횟수

    public boolean isAdmin() {
        return this.role == 0;
    }
}
