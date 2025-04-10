package com.NBE_4_5_2.Team5.global.exception

import com.NBE_4_5_2.Team5.global.dto.Empty
import com.NBE_4_5_2.Team5.global.response.RsData

open class ServiceException(
    private val _code: String,
    override val message: String
): RuntimeException(message) {

    private var rsData: RsData<Empty> = RsData(_code, message)

    val code: String
        get() = rsData.code

    val msg: String
        get() = rsData.message

    val statusCode: Int
        get() = rsData.statusCode

}
