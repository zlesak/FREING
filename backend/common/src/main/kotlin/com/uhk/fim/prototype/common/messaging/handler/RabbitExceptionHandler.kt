package com.uhk.fim.prototype.common.messaging.handler

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException
import org.springframework.stereotype.Component

@Component
class RabbitExceptionHandler : RabbitListenerErrorHandler {

    override fun handleError(
        p0: Message?,
        p1: Channel?,
        p2: org.springframework.messaging.Message<*>?,
        p3: ListenerExecutionFailedException?
    ): Any {
        return Any()
    }
}