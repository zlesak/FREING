package com.uhk.fim.prototype.common.exceptions

import com.uhk.fim.prototype.common.handlers.GlobalExceptionHandler
import com.uhk.fim.prototype.common.messaging.dto.ErrorProps
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

open class AbstractResponseException(
    final override val message: String,
    open val httpStatus: HttpStatus,
) : RuntimeException(){
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    init {
        logger.warn(message)
    }
}

fun Exception.getType() = this::class.java
fun Exception.getErrorProps() = ErrorProps(this.message?:"", getType())
