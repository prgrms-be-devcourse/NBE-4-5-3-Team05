package com.NBE_4_5_2.Team5.global.exception.security;

public class TokenNotValidException extends SecurityException {
	public TokenNotValidException(String code, String msg) {
		super(code, msg);
	}
}
