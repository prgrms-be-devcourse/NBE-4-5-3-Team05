package com.NBE_4_5_2.Team5.domain.notification.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.NBE_4_5_2.Team5.domain.notification.entity.Notification;
import com.NBE_4_5_2.Team5.domain.notification.service.NotificationService;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.infrastructure.kafka.KafkaConsumer;

@RestController
public class NotificationController {

	private KafkaConsumer kafkaConsumer;
	private NotificationService notificationService;
	private UserService userService;

	public NotificationController(KafkaConsumer kafkaConsumer, NotificationService notificationService,
		UserService userService) {
		this.kafkaConsumer = kafkaConsumer;
		this.notificationService = notificationService;
		this.userService = userService;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/api/notification/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId){
		SseEmitter emitter = new SseEmitter(0L);
		kafkaConsumer.addEmitter(emitter);


		if (lastEventId != null) {
			long lastId = Long.parseLong(lastEventId);
			List<Notification> missed = notificationService.getNotificationsAfter(lastId);

			for (Notification n : missed) {
				try {
					if(n.isGlobal() || (!n.isGlobal() && n.getUserId().equals(userService.getUserIdentity().getId()))){
						emitter.send(SseEmitter.event()
							.id(String.valueOf(n.getId()))
							.data(n.getContent()));
					}
				} catch (IOException e) {
					kafkaConsumer.removeEmitter(n.getUserId());
					break;
				}
			}
		}

		return emitter;
	}
}
