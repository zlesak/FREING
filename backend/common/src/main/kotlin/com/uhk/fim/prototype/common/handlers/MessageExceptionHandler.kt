package com.uhk.fim.prototype.common.handlers

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.aopalliance.intercept.MethodInterceptor
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
class MessageExceptionHandler(
    private val messageConverter: MessageConverter,
    private val activeMessagingManager: ActiveMessagingManager
) {
    private val logger = LoggerFactory.getLogger(MessageExceptionHandler::class.java)

    @Bean
    fun rabbitListenerAdvice(): MethodInterceptor = MethodInterceptor { invocation ->

        val payload = invocation.arguments.firstNotNullOfOrNull { arg ->
            when (arg) {
                is Message -> {
                    try {
                        messageConverter.fromMessage(arg) as? MessageResponse
                    } catch (e: Exception) {
                        logger.warn("[GlobalRabbitListenerAdvice] Failed to convert message: {}", e.message)
                        null
                    }
                }

                is MessageResponse -> arg
                else -> null
            }
        }

        payload?.let { message ->
            if (message.error != null) {
                logger.warn("[GlobalRabbitListenerAdvice] Message contains error: {}", message.error)
            } else {
                logger.info("[GlobalRabbitListenerAdvice] Message OK: {}", message)
            }
        }

        invocation.proceed()
    }

}