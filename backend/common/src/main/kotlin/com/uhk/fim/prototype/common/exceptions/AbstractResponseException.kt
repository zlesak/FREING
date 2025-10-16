package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class AbstractResponseException(
    override val message: String,
    open val httpStatus: HttpStatus,
) : RuntimeException()
