package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class MessageListenerProcessorChain(
    private val messageConverter: MessageConverter,
): MethodInterceptor {

    private val processors: MutableList<MessageListenerProcessor> = mutableListOf()


    override fun invoke(invocation: MethodInvocation): Any? {
        val inputData = excludeProcessorInputData(invocation.arguments)?: return invocation.proceed()

        processors.forEach { it.process(inputData) }

        return invocation.proceed()
    }

    fun registerProcessor(processor: MessageListenerProcessor): MessageListenerProcessorChain{
        println("[MessageListenerProcessorChain] registered new processor: ${processor::class.simpleName}")
        processors.add(processor)
        return this
    }

    private fun excludeProcessorInputData(args: Array<Any>): MessageProcess? {
        val payload = args.firstNotNullOfOrNull { arg ->
            when (arg) {
                is Message -> {
                    try {
                        MessageProcess(
                            messageResponse = messageConverter.fromMessage(arg) as? MessageResponse,
                            correlationId = arg.messageProperties.correlationId
                        )
                    } catch (e: Exception) {
                        println("[GlobalRabbitListenerAdvice] Failed to convert message: ${e.message}")
                        null
                    }
                }
                else -> null
            }
        }

        payload?.let { message ->
            // Here you can handle messages with embedded errors
            if (message.messageResponse?.error != null) {
                println("[GlobalRabbitListenerAdvice] Message contains error: ${message.messageResponse?.error}")
                // Optional: handle, modify, or route message to dead-letter queue
            } else {
                println("[GlobalRabbitListenerAdvice] Message OK: $message")
            }
        }

        return payload
    }
}

data class MessageProcess(
    var correlationId: String,
    var messageResponse: MessageResponse? = null,
)