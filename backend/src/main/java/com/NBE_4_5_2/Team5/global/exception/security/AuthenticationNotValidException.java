package com.NBE_4_5_2.Team5.global.exception.security;

public class AuthenticationNotValidException extends SecurityException {
	public AuthenticationNotValidException(String code, String message) {
		super(code, message);
	}
}
