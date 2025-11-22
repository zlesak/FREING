package com.uhk.fim.prototype.common.messaging.preprocessor

interface MessageListenerProcessor {
    fun process(message: MessageProcess): MessageProcess
}