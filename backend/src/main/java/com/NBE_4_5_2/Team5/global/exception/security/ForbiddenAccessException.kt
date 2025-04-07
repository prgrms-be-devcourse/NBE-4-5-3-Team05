package com.NBE_4_5_2.Team5.global.exception.security

class ForbiddenAccessException(code: String, message: String) :
    SecurityException(code, message)
