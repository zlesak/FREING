package com.uhk.fim.prototype.common.messaging.preprocessor

import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Service

@Service
class MessageExtractor(
    private val messageConverter: MessageConverter,
) {

    fun extractMessage(args: Array<Any>): MessageProcess? {
        val payload = args.firstNotNullOfOrNull { arg ->
            when (arg) {
                is Message -> {
                    try {
                        val convertedMessage = messageConverter.fromMessage(arg)
                        val correlationId = arg.messageProperties.correlationId

                        MessageProcess(
                            message = convertedMessage,
                            correlationId = correlationId
                        )
                    } catch (e: Exception) {
                        println("[MessageExtractor] Failed to convert message: ${e.message}")
                        null
                    }
                }

                else -> null
            }
        }

        payload?.let { message ->
            when {
                message.isResponse() -> {
                    val response = message.messageResponse
                    if (response?.error != null) {
                        println("[MessageExtractor] MessageResponse contains error: ${response.error}")
                    } else {
                        println("[MessageExtractor] MessageResponse OK: correlationId=${message.correlationId}")
                    }
                }

                message.isRequest() -> {
                    val request = message.messageRequest
                    println("[MessageExtractor] MessageRequest received: route=${request?.route}, correlationId=${message.correlationId}")
                }

                else -> {
                    println("[MessageExtractor] Unknown message type: ${message.message?.javaClass?.simpleName}")
                }
            }
        }

        return payload
    }
}