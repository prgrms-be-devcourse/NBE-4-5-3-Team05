package com.NBE_4_5_2.Team5.global.exception.notice;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;

public abstract class NoticeException extends ServiceException {
	public NoticeException(String code, String message) {
		super(code, message);
	}
}
