package com.NBE_4_5_2.Team5.global.exception.security;

public class ForbiddenAccessException extends SecurityException {
	public ForbiddenAccessException(String code, String message) {
		super(code, message);
	}
}
