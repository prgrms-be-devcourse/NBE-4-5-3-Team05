package com.NBE_4_5_2.Team5.global.exception.security

class TokenNotValidException(code: String, msg: String) :
    SecurityException(code, msg)
