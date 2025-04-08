package com.NBE_4_5_2.Team5.global.exception.notice

class NoticeNotFoundException : NoticeException {
    constructor() : super("404-1", "그런 Notice Post는 없습니다.")
    constructor(code: String, message: String) : super(code, message)
}
