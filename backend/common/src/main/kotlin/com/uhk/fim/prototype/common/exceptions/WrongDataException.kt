package com.uhk.fim.prototype.common.exceptions

import org.springframework.http.HttpStatus

open class WrongDataException(target: String? = null, message: String? = null)
    : AbstractResponseException(message?:"${target ?: "Provided data"} is invalid or inconsistent!", HttpStatus.BAD_REQUEST)