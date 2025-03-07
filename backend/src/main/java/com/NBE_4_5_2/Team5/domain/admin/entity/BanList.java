package com.NBE_4_5_2.Team5.domain.admin.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.NBE_4_5_2.Team5.domain.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
public class BanList {
	@Id
	private final String id = "ban-" + UUID.randomUUID();

	private String reason;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User bannedUser;

	@CreatedDate
	private LocalDateTime startDate;

	private LocalDateTime endDate;

	@Builder
	public BanList(String reason, User bannedUser, LocalDateTime endDate) {
		this.reason = reason;
		this.bannedUser = bannedUser;
		this.endDate = endDate;
	}
}
