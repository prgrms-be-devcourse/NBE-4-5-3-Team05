package com.NBE_4_5_2.Team5.global.exception.user

import com.NBE_4_5_2.Team5.global.exception.security.SecurityException

class WrongPasswordException(code: String, message: String) :
    SecurityException(code, message)
