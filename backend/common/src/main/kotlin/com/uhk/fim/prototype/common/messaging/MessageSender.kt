package com.uhk.fim.prototype.common.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.actions.IMessageAction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.util.*

@Component
class MessageSender(
    private val rabbitTemplate: RabbitTemplate,
    private val activeMessagingManager: ActiveMessagingManager
) {
    @Value("\${spring.application.name}")
    private val serviceName: String = ""

    val replyQueueName: String = "invoice.responses.$serviceName-" + UUID.randomUUID().toString()
    private val logger = LoggerFactory.getLogger(MessageSender::class.java)

    init {
        logger.info("replyQueueName = {}", replyQueueName)
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendRequest(
        request: MessageRequest<out IMessageAction>,
        timeoutSeconds: Long = 5,
        requestId: String? = null,
        correlationId: String? = null
    ): MessageResponse {
        return activeMessagingManager.registerMessage(
            timeoutSeconds = timeoutSeconds,
            correlationId = correlationId,
            requestId = requestId
        ) { messageIds ->
            logger.info("Sending request with requestId: {} with correlationId={}", messageIds.requestId, messageIds.correlationId)
            sendRequest(request, request.route, messageIds.correlationId)

        }
    }

    private fun sendRequest(request: MessageRequest<out IMessageAction>, route: String, correlationId: String? = null) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            route,
            request
        ) { message ->
            message.messageProperties.replyTo = replyQueueName
            message.messageProperties.correlationId = correlationId ?: request.requestId
            message
        }
    }

    fun sendResponse(response: MessageResponse, replyTo: String, correlationId: String) {
        logger.info("Sending response to {} with correlationId={}", replyTo, correlationId)
        rabbitTemplate.convertAndSend(
            "",
            replyTo,
            response
        ) { message ->
            message.messageProperties.correlationId = correlationId
            message
        }
    }

}