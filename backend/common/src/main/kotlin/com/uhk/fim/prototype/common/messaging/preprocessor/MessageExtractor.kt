package com.uhk.fim.prototype.common.messaging.preprocessor

import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class MessageExtractor(
    private val messageConverter: MessageConverter,
) {

    private val logger = LoggerFactory.getLogger(MessageExtractor::class.java)

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
                        logger.debug("[MessageExtractor] Failed to convert message: ${e.message}")
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
                        logger.debug("[MessageExtractor] MessageResponse contains error: {}", response.error)
                    } else {
                        logger.debug("[MessageExtractor] MessageResponse OK: correlationId=${message.correlationId}")
                    }
                }

                message.isRequest() -> {
                    val request = message.messageRequest
                    logger.debug("[MessageExtractor] MessageRequest received: route=${request?.route}, correlationId=${message.correlationId}")
                }

                else -> {
                    logger.debug("[MessageExtractor] Unknown message type: ${message.message?.javaClass?.simpleName}")
                }
            }
        }

        return payload
    }
}