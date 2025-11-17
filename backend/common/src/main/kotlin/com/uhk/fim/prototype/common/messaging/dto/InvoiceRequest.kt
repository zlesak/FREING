package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction

data class InvoiceRequest(
    override val apiSourceService: SourceService,
    override val requestId: String,
    override val targetId: Long? = null, //customerId, invoiceId etc.
    override val action: MessageInvoiceAction,
    override val payload: Map<String, Any> = emptyMap()
) : MessageRequest<MessageInvoiceAction>(apiSourceService, requestId, targetId, action, payload)
