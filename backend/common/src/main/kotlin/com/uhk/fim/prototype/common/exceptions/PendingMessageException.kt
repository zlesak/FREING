package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus


open class PendingMessageException(target: String? = null, message: String? = null)
    : AbstractResponseException(message?:"${target ?: "Message"} is still pending and cannot be processed!", HttpStatus.CONFLICT) {
}