package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.buildException
import org.springframework.stereotype.Component

@Component
class MessageExceptionPreprocessor(
    private val activeMessagingManager: ActiveMessagingManager
) : MessageListenerProcessor {

    override fun process(message: MessageProcess): MessageProcess {
        if (!message.isResponse()) {
            println("[MessageExceptionHandler] Skipping non-response message")
            return message
        }

        val response = message.messageResponse
        if (response?.error == null) {
            println("[MessageExceptionHandler] No error in response")
            return message
        }

        println("[MessageExceptionHandler] Handling message error: ${response.error.message}")
        activeMessagingManager.completeExceptionally(message.correlationId, response.error.buildException())
        return message
    }
}