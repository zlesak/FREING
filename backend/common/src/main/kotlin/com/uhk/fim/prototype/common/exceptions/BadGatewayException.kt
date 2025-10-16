package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

class BadGatewayException(text: String) : AbstractResponseException(text, HttpStatus.BAD_GATEWAY)