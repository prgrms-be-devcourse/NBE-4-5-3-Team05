package com.NBE_4_5_2.Team5.global.exception

import com.NBE_4_5_2.Team5.global.dto.RsData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException::class)
    @ResponseStatus
    fun serviceExceptionHandle(e: ServiceException): ResponseEntity<RsData<Void>> {
        return ResponseEntity
            .status(e.statusCode)
            .body(
                RsData(e.code, e.message)
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Void>> {
        val message = e.bindingResult.fieldErrors
            .map { fieldError -> "${fieldError.field} : ${fieldError.defaultMessage}" }
            .sorted()
            .joinToString("\n")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                RsData("400-1", message)
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<RsData<Void>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                RsData("400-1", "값을 입력해주세요.")
            )
    }
}
