package com.NBE_4_5_2.Team5.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification;
import com.NBE_4_5_2.Team5.domain.notification.repository.NotificationRepository;

@Service
public class NotificationService {

	private NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public List<Notification> getNotificationsAfter(Long lastId){

		return notificationRepository.findAllByIdAfter(lastId);
	}
}
