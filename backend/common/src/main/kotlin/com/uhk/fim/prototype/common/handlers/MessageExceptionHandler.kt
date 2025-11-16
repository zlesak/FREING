package com.uhk.fim.prototype.common.handlers

import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.aopalliance.intercept.MethodInterceptor
import org.aspectj.lang.annotation.Aspect
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class MessageExceptionHandler(
    private val messageConverter: MessageConverter,
) {

    @Bean
    fun rabbitListenerAdvice(): MethodInterceptor = MethodInterceptor { invocation ->

        val payload = invocation.arguments
            .mapNotNull { arg ->
                when (arg) {
                    is Message -> {
                        // Convert raw AMQP Message to your DTO
                        try {
                            messageConverter.fromMessage(arg) as? MessageResponse
                        } catch (e: Exception) {
                            println("[GlobalRabbitListenerAdvice] Failed to convert message: ${e.message}")
                            null
                        }
                    }
                    is MessageResponse -> arg
                    else -> null
                }
            }
            .firstOrNull()

        payload?.let { message ->
            // Here you can handle messages with embedded errors
            if (message.error != null) {
                println("[GlobalRabbitListenerAdvice] Message contains error: ${message.error}")
                // Optional: handle, modify, or route message to dead-letter queue
            } else {
                println("[GlobalRabbitListenerAdvice] Message OK: $message")
            }
        }

        // Proceed to the actual listener method
        invocation.proceed()
    }

}