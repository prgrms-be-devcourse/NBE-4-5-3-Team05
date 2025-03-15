package com.NBE_4_5_2.Team5.global.exception.user;

import com.NBE_4_5_2.Team5.global.exception.security.SecurityException;

public class WrongPasswordException extends SecurityException {
	public WrongPasswordException(String code, String message) {
		super(code, message);
	}
}
