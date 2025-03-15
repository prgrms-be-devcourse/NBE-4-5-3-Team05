package com.NBE_4_5_2.Team5.global.exception.post.product;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;

public abstract class ProductPostException extends ServiceException {
	public ProductPostException(String code, String message) {
		super(code, message);
	}
}
