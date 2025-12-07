package com.uhk.fim.prototype.common.messaging.preprocessor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MessageListenerPreprocessor: MessageListenerProcessor {

    private val logger = LoggerFactory.getLogger(MessageListenerPreprocessor::class.java)

    //should update coroutine context with correlationId
    override fun process(message: MessageProcess): MessageProcess {
        logger.debug("[MessageListenerPreprocessor] setting coroutine correlationId={}", message.correlationId)
        return message
    }
}