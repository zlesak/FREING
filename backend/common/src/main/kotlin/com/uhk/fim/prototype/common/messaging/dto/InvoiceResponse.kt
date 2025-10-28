package com.uhk.fim.prototype.common.messaging.dto

data class InvoiceResponse(
    val requestId: String,
    val invoiceId: Long? = null,
    val status: String, // např. "ok", "not_found", "error"
    val payload: Map<String, Any>? = null,
    val error: String? = null
)
