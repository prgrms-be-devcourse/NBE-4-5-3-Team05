package com.NBE_4_5_2.Team5.global.exception.post.category;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;

public abstract class CategoryException extends ServiceException {
	public CategoryException(String code, String message) {
		super(code, message);
	}
}
