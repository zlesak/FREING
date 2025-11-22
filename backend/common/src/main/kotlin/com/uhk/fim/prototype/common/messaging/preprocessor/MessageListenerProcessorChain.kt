package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.aopalliance.intercept.MethodInterceptor
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class MessageListenerProcessorChain(
    private val processors: List<MessageListenerProcessor>,
    private val messageConverter: MessageConverter,
    private val appScope: CoroutineScope,
) {


    @Bean
    @Qualifier("messagePreProcessorChain")
    fun messageListenerPreProcessorChain(): MethodInterceptor = MethodInterceptor { invocation ->
        val inputData = excludeProcessorInputData(invocation.arguments)?: return@MethodInterceptor invocation.proceed()

        processors.forEach { it.process(inputData) }

        appScope.launch {
            try {
                println("Running task in coroutine on thread: ${Thread.currentThread().name}")
                invocation.proceed()
            } catch (ex: Exception) {
                println("Error in coroutine task: $ex")
            }
        }
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