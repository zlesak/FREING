package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus


open class PendingMessageException(message: String?= null)
    : AbstractResponseException(message?: "Message is still pending and cannot be processed!", HttpStatus.CONFLICT) {
}