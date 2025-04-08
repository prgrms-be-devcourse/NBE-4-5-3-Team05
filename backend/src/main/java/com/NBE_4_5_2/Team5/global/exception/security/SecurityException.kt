package com.NBE_4_5_2.Team5.global.exception.security

import com.NBE_4_5_2.Team5.global.exception.ServiceException

abstract class SecurityException(code: String, message: String) :
    ServiceException(code, message)
