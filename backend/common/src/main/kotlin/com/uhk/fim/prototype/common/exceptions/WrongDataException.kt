package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

class WrongDataException(message: String) : AbstractResponseException(message, HttpStatus.BAD_REQUEST)