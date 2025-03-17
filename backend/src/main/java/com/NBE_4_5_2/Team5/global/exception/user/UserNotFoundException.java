package com.NBE_4_5_2.Team5.global.exception.user;

import com.NBE_4_5_2.Team5.global.exception.security.SecurityException;

public class UserNotFoundException extends SecurityException {
	public UserNotFoundException(String code, String message) {
		super(code, message);
	}
}
