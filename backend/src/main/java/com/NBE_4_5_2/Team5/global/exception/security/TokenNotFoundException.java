package com.NBE_4_5_2.Team5.global.exception.security;

public class TokenNotFoundException extends SecurityException {
	public TokenNotFoundException(String code, String msg) {
		super(code, msg);
	}
}
