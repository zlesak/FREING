package com.uhk.fim.prototype.common.handlers

import com.uhk.fim.prototype.common.exceptions.AbstractResponseException
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.OperationDeniedException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(OperationDeniedException::class)
    fun handleAccessDenied(ex: OperationDeniedException): ResponseEntity<String> = buildResponse(ex)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<String> = buildResponse(ex)
    @ExceptionHandler(WrongDataException::class)
    fun handleWrongData(ex: WrongDataException): ResponseEntity<String> = buildResponse(ex)

    @ExceptionHandler(Exception::class)
    fun handleOther(ex: Exception): ResponseEntity<String> {
        logger.error(ex.message, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.message)
    }

    //Pro externí služby zachytit chyby a převést na naše výjimky co máme
    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientError(ex: HttpClientErrorException): ResponseEntity<String> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        val message = ex.message
        return when (status) {
            HttpStatus.BAD_REQUEST,
            HttpStatus.CONFLICT,
            HttpStatus.UNPROCESSABLE_ENTITY -> buildResponse(WrongDataException(message = message))
            HttpStatus.UNAUTHORIZED,
            HttpStatus.FORBIDDEN -> buildResponse(OperationDeniedException(message = message))
            HttpStatus.NOT_FOUND -> buildResponse(NotFoundException(message = message))
            else -> ResponseEntity.status(status).body(message)
        }
    }

    fun buildResponse(ex: AbstractResponseException): ResponseEntity<String> {
        logger.warn("Message: ${ex.message} with status ${ex.httpStatus}")
        return ResponseEntity.status(ex.httpStatus).body(ex.message)
    }
}