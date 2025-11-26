package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class WrongDataException( message: String? = null)
    : AbstractResponseException(message?:  "Provided data invalid or inconsistent!", HttpStatus.BAD_REQUEST)