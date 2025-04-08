package com.NBE_4_5_2.Team5.global.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
// TODO: global/response에도 RsData가 있음 중복 제거해야함
class RsData<T>(
    val code: String,
    val message: String,
    val data: T
) {

    constructor(code: String, message: String) : this(code, message, Empty() as T)

    @get:JsonIgnore
    val statusCode: Int
        get() {
            val statusCodeStr =
                code.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            return statusCodeStr.toInt()
        }
}
