package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.buildException
import org.springframework.stereotype.Component

@Component
class MessageExceptionPreprocessor(
    private val activeMessagingManager: ActiveMessagingManager
) : MessageListenerProcessor {


    //handle all error from messages
    override fun process(message: MessageProcess): MessageProcess {
        if (message.messageResponse == null || message.messageResponse?.error == null) return message

        println("[MessageExceptionHandler] handling message errors")
        activeMessagingManager.completeExceptionally(message.correlationId, message.messageResponse!!.error!!.buildException())
        return message
    }
}