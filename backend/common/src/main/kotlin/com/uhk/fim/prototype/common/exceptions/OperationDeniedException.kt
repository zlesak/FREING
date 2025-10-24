package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

class OperationDeniedException(text: String): AbstractResponseException(text, HttpStatus.FORBIDDEN)