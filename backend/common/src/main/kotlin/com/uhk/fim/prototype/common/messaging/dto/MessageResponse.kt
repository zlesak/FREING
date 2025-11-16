package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService

class MessageResponse(
    val apiSourceService: SourceService,
    val sourceService: SourceService,
    val requestId: String,
    val targetId: Long? = null, //customerId, invoiceId etc.
    val status: MessageStatus,
    val payload: Map<String, Any?> = emptyMap(),
    val error: String? = null
)
