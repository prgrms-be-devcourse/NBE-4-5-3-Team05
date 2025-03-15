package com.NBE_4_5_2.Team5.domain.user.user.service.email.service;

import org.springframework.stereotype.Component;

import com.NBE_4_5_2.Team5.global.config.email.TimeProvider;

@Component
public class DefaultTimeProvider implements TimeProvider {
	@Override
	public void sleep(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}
}
