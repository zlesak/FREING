package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class BadGatewayException(target: String? = null, message: String? = null)
    : AbstractResponseException(message ?: "${target ?: "Upstream service"} returned an invalid response!", HttpStatus.BAD_GATEWAY)