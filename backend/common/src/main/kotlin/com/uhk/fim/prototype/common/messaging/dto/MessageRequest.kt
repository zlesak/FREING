package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.SourceService

open class MessageRequest<T>(
    open val apiSourceService: SourceService,
    override val requestId: String,
    open val targetId: Long? = null, //customerId, invoiceId etc.
    open val action: T,
    open val payload: Map<String, Any>? = null
) : CommonMessageRequest(requestId)


open class CommonMessageRequest(
    open val requestId: String,
) {

    fun copy(requestId: String): CommonMessageRequest {
       return CommonMessageRequest(requestId)
    }
}
