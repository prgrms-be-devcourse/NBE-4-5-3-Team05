package com.NBE_4_5_2.Team5.global.exception.post;

import com.NBE_4_5_2.Team5.global.exception.post.category.CategoryException;

public class CategoryNotFoundException extends CategoryException {
	public CategoryNotFoundException(String code, String message) {
		super(code, message);
	}
}
