package com.NBE_4_5_2.Team5.global.exception.notice

import com.NBE_4_5_2.Team5.global.exception.ServiceException

abstract class NoticeException(code: String, message: String) :
    ServiceException(code, message)
