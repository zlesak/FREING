package com.uhk.fim.prototype.common.handlers

import com.uhk.fim.prototype.common.exceptions.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(OperationDeniedException::class)
    fun handleOperationDenied(ex: OperationDeniedException): ResponseEntity<String> {
        return buildResponse(ex)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<String> {
      return buildResponse(ex)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAccessDenied(ex: AuthorizationDeniedException): ResponseEntity<String> {
        return buildResponse(ex, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(WrongDataException::class)
    fun handleWrongData(ex: WrongDataException): ResponseEntity<String> {
        return buildResponse(ex)
    }

    @ExceptionHandler(BadGatewayException::class)
    fun handleBadGateway(ex: BadGatewayException): ResponseEntity<String> {
        return buildResponse(ex)
    }

    @ExceptionHandler(PendingMessageException::class)
    fun handlePendingException(ex: PendingMessageException): ResponseEntity<String> {
        return buildResponse(ex)
    }

    @ExceptionHandler(Exception::class)
    fun handleOther(ex: Exception): ResponseEntity<String> {
        logger.error(ex.message, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.message)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientError(ex: HttpClientErrorException): ResponseEntity<String> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        val message = ex.message
        return when (status) {
            HttpStatus.BAD_REQUEST,
            HttpStatus.CONFLICT,
            HttpStatus.UNPROCESSABLE_ENTITY -> buildResponse(WrongDataException(message))
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

    fun buildResponse(ex: Exception, status: HttpStatus): ResponseEntity<String> {
        logger.warn("Message: ${ex.message} with status ${status}")
        return ResponseEntity.status(status).body(ex.message)
    }
}