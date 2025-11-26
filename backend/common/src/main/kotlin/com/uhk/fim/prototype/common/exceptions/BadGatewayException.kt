package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class BadGatewayException(message: String? = null)
    : AbstractResponseException(message?: "Gateway returned an invalid response!", HttpStatus.BAD_GATEWAY)