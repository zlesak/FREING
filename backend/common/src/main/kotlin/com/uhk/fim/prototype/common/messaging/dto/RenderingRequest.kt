package com.uhk.fim.prototype.common.messaging.dto

data class RenderingRequest(
    val requestId: String,
    val documentId: Long? = null,
    val action: String, // např. "renderInvoice"
    val payload: Map<String, Any>? = null
)
