package com.NBE_4_5_2.Team5.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.NBE_4_5_2.Team5.global.response.RsData;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ServiceException.class)
	@ResponseStatus // Spring Docs 핸들러 인식
	public ResponseEntity<RsData<Void>> serviceExceptionHandle(ServiceException exception) {
		return ResponseEntity
			.status(exception.getStatusCode())
			.body(
				new RsData<>(
					exception.getCode(),
					exception.getMsg()
				)
			);
	}
}
