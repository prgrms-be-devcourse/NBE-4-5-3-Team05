package com.NBE_4_5_2.Team5.domain.user.admin.entity;

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "notice_post")
public class NoticePost extends BaseTime {

	@Id
	@EqualsAndHashCode.Include
	@Column(updatable = false, nullable = false)
	private String id = "npost-" + UUID.randomUUID();

	@ManyToOne
	private User admin;

	private String title;
	private String content;

	public NoticePost( String title, String content, User admin) {
		this.title = title;
		this.content = content;
		this.admin = admin;
	}

	public NoticePost update(String title, String content) {
		this.title = title;
		this.content = content;
		return this;
	}
}

