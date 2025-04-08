package com.NBE_4_5_2.Team5.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestErrorController {
    @GetMapping("/bad-request")
    fun triggerBadRequest() {
        throw IllegalArgumentException()
    }

    @GetMapping("/nullPointer-error")
    fun triggerNullPointerException() {
        throw NullPointerException()
    }
}
