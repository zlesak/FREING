package com.uhk.fim.prototype.common.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.dto.PaymentRequest
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.support.converter.MessageConversionException
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class ObjectMapperMessageConverter(private val objectMapper: ObjectMapper): MessageConverter {

    override fun toMessage(obj: Any, messageProperties: MessageProperties): Message {
        return try {
            val jsonBytes = objectMapper.writeValueAsBytes(obj)
            Message(jsonBytes, messageProperties)
        } catch (e: Exception) {
            throw MessageConversionException("Failed to convert object to message", e)
        }
    }

    override fun fromMessage(message: Message): Any {
        val rawJson = String(message.body, Charsets.UTF_8)
        val jsonNode = try {
            objectMapper.readTree(rawJson)
        } catch (e: Exception) {
            throw MessageConversionException("Failed to convert message body to object", e)
        }

        if (jsonNode !is ObjectNode) {
            throw MessageConversionException("Message body is not a JSON object")
        }

        val typeId = jsonNode["@class"]?.asText()
            ?: throw MessageConversionException("No @class property found in message")

        return try {
            when {
                typeId.contains("MessageResponse") -> objectMapper.treeToValue(jsonNode, MessageResponse::class.java)
                typeId.contains("CustomerRequest") -> objectMapper.treeToValue(jsonNode, CustomerRequest::class.java)
                typeId.contains("InvoiceRequest") -> objectMapper.treeToValue(jsonNode, InvoiceRequest::class.java)
                typeId.contains("PaymentRequest") -> objectMapper.treeToValue(jsonNode, PaymentRequest::class.java)
                else -> jsonNode
            }
        } catch (e: Exception) {
            throw MessageConversionException("Failed to convert JsonNode to value for type $typeId", e)
        }

    }
}