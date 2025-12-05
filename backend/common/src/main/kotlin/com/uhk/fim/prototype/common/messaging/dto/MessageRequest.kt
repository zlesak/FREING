package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.SourceService

abstract class MessageRequest<T>(
    open val apiSourceService: SourceService,
    override val requestId: String,
    open val targetId: Long? = null, //customerId, invoiceId etc.
    open val action: T,
    open val payload: Map<String, Any>? = null
) : CommonMessage<MessageRequest<T>>(requestId)


abstract class CommonMessage<T : CommonMessage<T>>(
    open val requestId: String
) {
    abstract fun copy(requestId: String): T
}