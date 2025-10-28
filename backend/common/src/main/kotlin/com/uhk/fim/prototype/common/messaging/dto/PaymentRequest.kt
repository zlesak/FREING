package com.uhk.fim.prototype.common.messaging.dto

data class PaymentRequest(
    val requestId: String,
    val paymentId: Long? = null,
    val action: String, // např. "getById", "pay"
    val payload: Map<String, Any>? = null
)
