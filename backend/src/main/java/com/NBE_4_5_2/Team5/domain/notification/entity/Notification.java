package com.NBE_4_5_2.Team5.domain.notification.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String userId;
	private boolean global;
	private String content;

	public Long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isGlobal() {
		return global;
	}

	public String getContent() {
		return content;
	}

	public Notification() {
	}

	public Notification(String userId, boolean global, String content) {
		this.userId = userId;
		this.global = global;
		this.content = content;
	}
}
