package com.NBE_4_5_2.Team5.domain.user.entity;

import com.NBE_4_5_2.Team5.global.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
public class User extends BaseTime {

    @Column(length = 20, nullable = false, unique = true)
    private String username;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 100, unique = true)
    private String refreshToken;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(length = 255)
    private String address;

    @Column(name = "profile_url", length = 255)
    private String profileUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer role = 1;  // 0: Admin, 1: 일반 유저

    @Column(nullable = false)
    @Builder.Default
    private Boolean blocked = false;

    @Column(name = "blocked_count", nullable = false)
    @Builder.Default
    private Integer blockedCount = 0;

    public boolean isAdmin() {
        return this.role == 0;
    }
}
