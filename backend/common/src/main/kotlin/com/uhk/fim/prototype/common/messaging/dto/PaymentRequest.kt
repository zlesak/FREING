package com.uhk.fim.prototype.common.messaging.dto

import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.payment.MessagePaymentAction

data class PaymentRequest(
    override val apiSourceService: SourceService,
    override val requestId: String, //customerId, invoiceId etc.
    override val targetId: Long? = null,
    override val action: MessagePaymentAction,
    override val payload: Map<String, Any>? = null
) : MessageRequest<MessagePaymentAction>(apiSourceService, requestId, targetId, action, payload)
