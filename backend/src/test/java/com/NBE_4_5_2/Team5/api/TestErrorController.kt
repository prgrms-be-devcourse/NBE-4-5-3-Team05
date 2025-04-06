package com.NBE_4_5_2.Team5.api;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestErrorController {

    @GetMapping("/bad-request")
    public void triggerBadRequest() {
        throw new IllegalArgumentException();
    }

    @GetMapping("/nullPointer-error")
    public void triggerNullPointerException() {
        throw new NullPointerException();
    }
}
