package com.uhk.fim.prototype.common.messaging.dto

data class PaymentResponse(
    val requestId: String,
    val paymentId: Long? = null,
    val status: String, // např. "ok", "not_found", "error"
    val payload: Map<String, Any>? = null,
    val error: String? = null
)
