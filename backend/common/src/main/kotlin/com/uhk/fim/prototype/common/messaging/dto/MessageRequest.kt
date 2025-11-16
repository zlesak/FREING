package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.SourceService

open class MessageRequest<T>(
    open val apiSourceService: SourceService,
    open val requestId: String,
    open val targetId: Long? = null, //customerId, invoiceId etc.
    open val action: T,
    open val payload: Map<String, Any>? = null
)
