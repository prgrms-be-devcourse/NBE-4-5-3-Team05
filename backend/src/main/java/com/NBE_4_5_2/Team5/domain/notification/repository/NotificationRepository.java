package com.NBE_4_5_2.Team5.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, String> {
	List<Notification> findAllByIdAfter(Long idAfter);
}
