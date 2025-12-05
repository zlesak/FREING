package com.uhk.fim.prototype.common.messaging.preprocessor

import com.uhk.fim.prototype.common.messaging.ObjectMapperMessageConverter
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.springframework.amqp.core.Message
import org.springframework.stereotype.Service

@Service
class MessageExtractor(
   // private val messageConverter: MessageConverter,
    private val messageConverter: ObjectMapperMessageConverter,
) {

    fun extractMessage(args: Array<Any>): MessageProcess? {
        val message = args.firstNotNullOfOrNull { it as? Message } ?: return null

        val correlationId = message.messageProperties.correlationId

        val rawJson = String(message.body, Charsets.UTF_8)

        return try {
            val convertedMessage = messageConverter.fromMessage(message) as? MessageResponse
            if (convertedMessage?.error != null) {
                println("[GlobalRabbitListenerAdvice] Message contains error: ${convertedMessage.error.message}")
            } else {
                println("[GlobalRabbitListenerAdvice] Message OK: $rawJson")
            }

           MessageProcess(
                messageResponse = convertedMessage,
                correlationId = correlationId
            )
        } catch (ex: Exception){
            println("[GlobalRabbitListenerAdvice] Message for preprocessing is has unsupported class! ${ex.message}")
            MessageProcess(
                messageResponse = null,
                correlationId = correlationId
            )
        }


    }
}