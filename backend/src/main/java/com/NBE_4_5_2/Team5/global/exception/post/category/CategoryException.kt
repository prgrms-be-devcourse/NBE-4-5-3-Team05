package com.NBE_4_5_2.Team5.global.exception.post.category

import com.NBE_4_5_2.Team5.global.exception.ServiceException

abstract class CategoryException(code: String, message: String) :
    ServiceException(code, message)
