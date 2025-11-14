package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class NotFoundException(target: String? = null, message: String? = null)
    : AbstractResponseException(message?: "${target ?: "Requested resource"} was not found!", HttpStatus.NOT_FOUND)
