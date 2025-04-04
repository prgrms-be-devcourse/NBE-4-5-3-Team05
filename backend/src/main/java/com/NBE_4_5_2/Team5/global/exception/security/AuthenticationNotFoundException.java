package com.NBE_4_5_2.Team5.global.exception.security;

public class AuthenticationNotFoundException extends SecurityException {
	public AuthenticationNotFoundException(String code, String msg) {
		super(code, msg);
	}
}
