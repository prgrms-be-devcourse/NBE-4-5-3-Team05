package com.NBE_4_5_2.Team5.global.exception.validation

import com.NBE_4_5_2.Team5.global.exception.ServiceException

class AlreadyUsedException(code: String, message: String) :
    ServiceException(code, message)
