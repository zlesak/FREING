package com.uhk.fim.prototype.common.messaging.dto

data class CustomerRequest(
    val requestId: String,
    val customerId: Long? = null,
    val action: String, // např. "get", "create", "update"
    val payload: Map<String, Any>? = null
)
