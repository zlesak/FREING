package com.uhk.fim.prototype.common.messaging.dto

data class InvoiceRequest(
    val requestId: String,
    val invoiceId: Long? = null,
    val action: String, // např. "getById", "create"
    val payload: Map<String, Any>? = null
)
