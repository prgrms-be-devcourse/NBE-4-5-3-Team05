package com.NBE_4_5_2.Team5.global.response

import org.springframework.data.domain.Page

data class PageDto<T>(
    val items: List<T>,
    val totalPages: Int,
    val totalItems: Int,
    val curPageNo: Int,
    val pageSize: Int
) {
    constructor(page: Page<T>) : this(
        items = page.content,
        totalPages = page.totalPages,
        totalItems = page.totalElements.toInt(),
        curPageNo = page.number + 1,
        pageSize = page.size
    )
}
