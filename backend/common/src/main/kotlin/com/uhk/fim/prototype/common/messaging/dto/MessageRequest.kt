package com.uhk.fim.prototype.common.messaging.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.uhk.fim.prototype.common.messaging.dto.deserializers.MessageRequestDeserializer

@JsonDeserialize(using = MessageRequestDeserializer::class)
data class MessageRequest<T>(
    val route: String,
    val requestId: String,
    val targetId: Long? = null, //customerId, invoiceId etc.
    val action: T,
    val payload: Map<String, Any>? = null
)
