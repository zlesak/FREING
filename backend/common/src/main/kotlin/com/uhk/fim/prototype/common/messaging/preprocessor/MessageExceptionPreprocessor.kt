package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.aopalliance.intercept.MethodInterceptor
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class MessageExceptionPreprocessor(
    private val activeMessagingManager: ActiveMessagingManager
) : MessageListenerProcessor {


    //handle all error from messages
    override fun process(message: MessageProcess): MessageProcess {
        println("[MessageExceptionHandler] handling message errors")
        return message
    }

}