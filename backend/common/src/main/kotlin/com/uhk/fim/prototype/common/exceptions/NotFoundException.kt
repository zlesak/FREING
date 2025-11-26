package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class NotFoundException(message: String?= null)
    : AbstractResponseException(message?: "Target was not found!", HttpStatus.NOT_FOUND)
