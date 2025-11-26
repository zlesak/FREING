package com.uhk.fim.prototype.common.messaging.preprocessor

import org.springframework.stereotype.Service

@Service
class MessageListenerPreprocessor: MessageListenerProcessor {

    //should update coroutine context with correlationId
    override fun process(message: MessageProcess): MessageProcess {
        println("[MessageListenerPreprocessor] setting coroutine correlationId=${message.correlationId}")
        return message
    }
}