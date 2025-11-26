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
        println("[MessageExceptionHandler] handling message errors")
        if (message.messageResponse == null || message.messageResponse?.error == null) return message

        activeMessagingManager.completeExceptionally(message.correlationId, message.messageResponse!!.error!!.buildException())
        return message
    }
}