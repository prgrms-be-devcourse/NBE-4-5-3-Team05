package com.NBE_4_5_2.Team5.global.exception.post.product

import com.NBE_4_5_2.Team5.global.exception.ServiceException

abstract class ProductPostException(code: String, message: String) :
    ServiceException(code, message)
