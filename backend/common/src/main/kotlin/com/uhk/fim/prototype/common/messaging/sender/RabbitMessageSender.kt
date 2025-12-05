package com.uhk.fim.prototype.common.messaging.sender

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.CommonMessage
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitMessageSender(
    rabbitTemplate: RabbitTemplate,
    val activeMessagingManager: ActiveMessagingManager,
    private val objectMapper: ObjectMapper
) {
    val rabbitSender = RabbitSender(rabbitTemplate, objectMapper)

    final inline fun <reified T : CommonMessage<T>> sendRequest(
        request: T,
        destination: String,
        replyQueueName: String,
        correlationId: String? = null,
        timeoutSeconds: Long = 5
    ): MessageResponse {
        return activeMessagingManager.registerMessage(
            timeoutSeconds = timeoutSeconds,
            requestId = request.requestId,
            correlationId = correlationId
        ) { messageIds ->
            println("Sending requestId: ${messageIds.requestId} with correlationId=${messageIds.correlationId}")
            rabbitSender.sendCommonRequest(request.copy(messageIds.requestId), destination, messageIds.correlationId, replyQueueName)
        }

    }

    final inline fun <reified T : CommonMessage<T>> sendResponse(
        request: T,
        replyTo: String,
        correlationId: String,
    ) {
        rabbitSender.sendCommonResponse(request, correlationId, replyTo)
    }


    class RabbitSender(val rabbitTemplate: RabbitTemplate, val objectMapper: ObjectMapper) {
       inline fun <reified T : CommonMessage<T>> send(
            request: T,
            exchange: String,
            destination: String,
            correlationId: String,
            replyQueueName: String?= null
        ) {
            val jsonNode = objectMapper.valueToTree<ObjectNode>(request)
            jsonNode.put("@class", request::class.java.name)
            rabbitTemplate.convertAndSend(
                exchange,
                destination,
                jsonNode
            ) { message ->
                message.messageProperties.correlationId = correlationId
                replyQueueName?.let { message.messageProperties.replyTo = replyQueueName }
                message
            }
        }

        inline fun <reified T : CommonMessage<T>> sendCommonRequest(
            request: T,
            destination: String,
            correlationId: String,
            replyQueueName: String
        ) {
            println("Sending ${T::class.java} request with correlationId=$correlationId")
            send(request, RabbitConfig.EXCHANGE, destination, correlationId, replyQueueName)
        }

        inline fun <reified T : CommonMessage<T>> sendCommonResponse(
            request: T,
            correlationId: String,
            replyTo: String
        ) {
            println("Sending ${T::class.java} response with correlationId=$correlationId")
            send(request, "", replyTo, correlationId)

        }
    }
}