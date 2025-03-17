package com.NBE_4_5_2.Team5.global.exception.notice;

public class NoticeNotFoundException extends NoticeException {
	public NoticeNotFoundException(String code, String message) {
		super(code, message);
	}

	public NoticeNotFoundException() {
		super("404-1", "그런 Notice Post는 없습니다.");
	}
}
