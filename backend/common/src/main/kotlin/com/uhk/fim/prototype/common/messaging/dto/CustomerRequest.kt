package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.customer.MessageCustomerAction

data class CustomerRequest(
    override val apiSourceService: SourceService,
    override val requestId: String,
    override val targetId: Long? = null, //customerId, invoiceId etc.
    override val action: MessageCustomerAction,
    override val payload: Map<String, Any>? = null
) : MessageRequest<MessageCustomerAction>(apiSourceService, requestId, targetId, action, payload)
