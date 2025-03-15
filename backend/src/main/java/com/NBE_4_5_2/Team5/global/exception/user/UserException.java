package com.NBE_4_5_2.Team5.global.exception.user;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;

public abstract class UserException extends ServiceException {
	public UserException(String code, String message) {
		super(code, message);
	}
}
