package com.NBE_4_5_2.Team5.domain.notification.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.notification.entity.Notification;
import com.NBE_4_5_2.Team5.infrastructure.kafka.KafkaNotificationProducerService;

@Aspect
public class NotificationAspect {


}
