package com.uhk.fim.prototype.common.messaging.sender

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.CommonMessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitMessageSender(
    rabbitTemplate: RabbitTemplate,
    val activeMessagingManager: ActiveMessagingManager
) {
    val rabbitSender = RabbitSender(rabbitTemplate)

    final inline fun <reified T : CommonMessageRequest> sendRequest(
        request: T,
        requestId: String,
        destination: String,
        correlationId: String,
        replyQueueName: String,
        timeoutSeconds: Long = 5
    ): MessageResponse {
        return activeMessagingManager.registerMessage(
            timeoutSeconds = timeoutSeconds,
            requestId = requestId,
            correlationId = correlationId
        ) { messageIds ->
            println("[invoice-service] Sending invoice requestId: ${messageIds.requestId} with correlationId=${messageIds.correlationId}")
            rabbitSender.sendCommonRequest(request.copy(messageIds.requestId), destination, messageIds.correlationId, replyQueueName)
        }

    }

    final inline fun <reified T : Any> sendResponse(
        request: T,
        destination: String,
        correlationId: String,
    ) {
        rabbitSender.sendCommonResponse(request, destination, correlationId)
    }


    class RabbitSender(private val rabbitTemplate: RabbitTemplate) {
        fun <T : Any> send(
            request: T,
            exchange: String,
            destination: String,
            correlationId: String,
            replyQueueName: String?= null
        ) {
            rabbitTemplate.convertAndSend(
                exchange,
                destination,
                request
            ) { message ->
                message.messageProperties.correlationId = correlationId
                replyQueueName?.let { message.messageProperties.replyTo = replyQueueName }
                message
            }
        }

        inline fun <reified T : Any> sendCommonRequest(
            request: T,
            destination: String,
            correlationId: String,
            replyQueueName: String
        ) {
            println("Sending ${T::class.java} request with correlationId=$correlationId")
            send(request, RabbitConfig.EXCHANGE, destination, correlationId, replyQueueName)
        }

        inline fun <reified T : Any> sendCommonResponse(
            request: T,
            correlationId: String,
            replyTo: String
        ) {
            println("Sending ${T::class.java} response with correlationId=$correlationId")
            send(request, "", replyTo, correlationId)

        }
    }
}