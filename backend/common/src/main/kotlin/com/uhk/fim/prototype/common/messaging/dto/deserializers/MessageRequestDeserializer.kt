package com.uhk.fim.prototype.common.messaging.dto.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.actions.CustomerMessageAction
import com.uhk.fim.prototype.common.messaging.enums.actions.IMessageAction
import com.uhk.fim.prototype.common.messaging.enums.actions.InvoiceMessageAction
import com.uhk.fim.prototype.common.messaging.enums.actions.PaymentMessageAction
import com.uhk.fim.prototype.common.messaging.enums.actions.RenderMessageAction

class MessageRequestDeserializer : JsonDeserializer<MessageRequest<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MessageRequest<*> {
        val mapper = p.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(p)

        val route = node.get("route").asText()
        val requestId = node.get("requestId").asText()
        val targetId = node.get("targetId")?.asLong()
        val actionNode = node.get("action")
        val payloadNode = node.get("payload")

        val actionClass = when (route) {
            "customer.requests" -> CustomerMessageAction::class.java
            "invoice.requests" -> InvoiceMessageAction::class.java
            "payment.requests" -> PaymentMessageAction::class.java
            "rendering.requests" -> RenderMessageAction::class.java
            else -> throw IllegalArgumentException("Unknown route: $route")
        }

        val action: IMessageAction = if (actionNode.isTextual) {
            val actionString = actionNode.asText()
            actionClass.enumConstants.first { it.name == actionString }
        } else {
            mapper.treeToValue(actionNode, actionClass)
        }

        val payload: Map<String, Any>? = if (payloadNode != null && !payloadNode.isNull) {
            mapper.convertValue(payloadNode, mapper.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                Any::class.java
            ))
        } else null

        return MessageRequest(
            route = route,
            requestId = requestId,
            targetId = targetId,
            action = action,
            payload = payload
        )
    }
}

