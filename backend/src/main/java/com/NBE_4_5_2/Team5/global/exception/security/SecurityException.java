package com.NBE_4_5_2.Team5.global.exception.security;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;

public abstract class SecurityException extends ServiceException {
	public SecurityException(String code, String message) {
		super(code, message);
	}
}
