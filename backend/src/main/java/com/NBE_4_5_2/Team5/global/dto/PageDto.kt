package com.NBE_4_5_2.Team5.global.dto

import org.springframework.data.domain.Page

class PageDto<T>(
    val items: List<T>,
    val totalPages: Int,
    val totalItems: Int,
    val currentPageNo: Int,
    val pageSize: Int
) {

    constructor(postPage: Page<T>): this(
        items = postPage.content,
        totalPages = postPage.totalPages,
        totalItems = postPage.totalElements.toInt(),
        currentPageNo = postPage.number + 1,
        pageSize = postPage.size
    )
}

