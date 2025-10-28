package com.uhk.fim.prototype.common.messaging.dto

data class RenderingResponse(
    val requestId: String,
    val documentId: Long? = null,
    val status: String, // např. "ok", "error"
    val payload: Map<String, Any>? = null,
    val error: String? = null
)
