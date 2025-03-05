package com.NBE_4_5_2.Team5.domain.user.entity;

import com.NBE_4_5_2.Team5.global.entity.BaseTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
	@Enumerated(EnumType.)
	private Role role; // 0: admin , 1: 일반 유저

	private boolean blocked;
	private int blockedCount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean blocked = false;

    @Column(name = "blocked_count", nullable = false)
    @Builder.Default
    private Integer blockedCount = 0;
	public User(String username, String password, String email, String nickname, String address, String profileUrl,
		Role role,
		LocalDateTime createdAt, LocalDateTime modifiedAt) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.blocked = false;
		this.blockedCount = 0;
		this.nickname = nickname;
		this.address = address;
		this.profileUrl = profileUrl;
		this.role = role;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

    public boolean isAdmin() {
        return this.role == 0;
    }
	public void ban() {
		this.blocked = true;
		this.blockedCount++;
	}

	public void unBan() {

		if (!this.blocked) {
			return;
		}
		blocked = false;
	}
}
