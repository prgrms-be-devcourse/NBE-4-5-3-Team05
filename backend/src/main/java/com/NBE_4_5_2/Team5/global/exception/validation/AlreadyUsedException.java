package com.NBE_4_5_2.Team5.global.exception.validation;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;

public class AlreadyUsedException extends ServiceException {
	public AlreadyUsedException(String code, String message) {
		super(code, message);
	}
}
