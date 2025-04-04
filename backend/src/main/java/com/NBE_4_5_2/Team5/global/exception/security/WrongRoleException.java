package com.NBE_4_5_2.Team5.global.exception.security;

public class WrongRoleException extends SecurityException {
	public WrongRoleException(String code, String message) {
		super(code, message);
	}
}
