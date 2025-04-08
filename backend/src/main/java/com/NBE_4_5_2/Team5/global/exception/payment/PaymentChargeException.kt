package com.NBE_4_5_2.Team5.global.exception.payment

import com.NBE_4_5_2.Team5.global.exception.ServiceException

class PaymentChargeException(code: String, message: String) :
    ServiceException(code, message)
