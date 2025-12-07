package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.buildException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MessageExceptionPreprocessor(
    private val activeMessagingManager: ActiveMessagingManager
) : MessageListenerProcessor {

    private val logger = LoggerFactory.getLogger(MessageExceptionPreprocessor::class.java)

    override fun process(message: MessageProcess): MessageProcess {
        if (!message.isResponse()) {
            logger.debug("[MessageExceptionHandler] Skipping non-response message")
            return message
        }

        val response = message.messageResponse
        if (response?.error == null) {
            logger.debug("[MessageExceptionHandler] No error in response")
            return message
        }

        logger.debug("[MessageExceptionHandler] Handling message error: ${response.error.message}")
        activeMessagingManager.completeExceptionally(message.correlationId, response.error.buildException())
        return message
    }
}