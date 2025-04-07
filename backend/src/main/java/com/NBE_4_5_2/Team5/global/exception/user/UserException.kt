package com.NBE_4_5_2.Team5.global.exception.user

import com.NBE_4_5_2.Team5.global.exception.ServiceException

abstract class UserException(code: String, message: String) :
    ServiceException(code, message)
