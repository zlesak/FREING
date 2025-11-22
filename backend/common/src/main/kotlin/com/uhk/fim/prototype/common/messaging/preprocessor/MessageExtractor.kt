package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
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
                        MessageProcess(
                            messageResponse = messageConverter.fromMessage(arg) as? MessageResponse,
                            correlationId = arg.messageProperties.correlationId
                        )
                    } catch (e: Exception) {
                        println("[GlobalRabbitListenerAdvice] Failed to convert message: ${e.message}")
                        null
                    }
                }
                else -> null
            }
        }

        payload?.let { message ->
            // Here you can handle messages with embedded errors
            if (message.messageResponse?.error != null) {
                println("[GlobalRabbitListenerAdvice] Message contains error: ${message.messageResponse?.error}")
                // Optional: handle, modify, or route message to dead-letter queue
            } else {
                println("[GlobalRabbitListenerAdvice] Message OK: $message")
            }
        }

        return payload
    }
}