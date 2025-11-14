package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class OperationDeniedException(target: String? = null, message: String? = null)
    : AbstractResponseException(message?: "Operation denied${if (target != null) ": $target" else "!"}", HttpStatus.FORBIDDEN)