package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class OperationDeniedException(message: String?= null)
    : AbstractResponseException(message?: "Operation denied!", HttpStatus.FORBIDDEN)