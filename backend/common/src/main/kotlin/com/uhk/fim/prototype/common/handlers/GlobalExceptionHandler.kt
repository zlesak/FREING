package com.uhk.fim.prototype.common.handlers

import com.uhk.fim.prototype.common.exceptions.AbstractResponseException
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.OperationDeniedException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OperationDeniedException::class)
    fun handleAccessDenied(ex: OperationDeniedException): ResponseEntity<String> {
        return buildResponse(ex)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleAccessDenied(ex: NotFoundException): ResponseEntity<String> {
        return buildResponse(ex)
    }

    @ExceptionHandler(WrongDataException::class)
    fun handleAccessDenied(ex: WrongDataException): ResponseEntity<String> {
        return buildResponse(ex)
    }

    fun buildResponse(ex: AbstractResponseException): ResponseEntity<String> {
        return ResponseEntity.status(ex.httpStatus).body(ex.message)
    }
}