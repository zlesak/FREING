package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

class NotFoundException(text: String) : AbstractResponseException(text, HttpStatus.NOT_FOUND)