package com.uhk.fim.prototype.common.messaging

import com.uhk.fim.prototype.common.exceptions.BadGatewayException
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.getErrorProps
import com.uhk.fim.prototype.common.extensions.processInCoroutine
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.actions.IMessageAction
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter

abstract class AbstractMessageListener<T : IMessageAction>(
    private val messageConverter: MessageConverter,
    private val messageSender: MessageSender,
    private val invalidMessageActionHandler: InvalidMessageActionHandler,
    private val activeMessagingManager: ActiveMessagingManager,
    private val rabbitScope: CoroutineScope,
    private val actionClass: Class<T>
) {
    private val logger = LoggerFactory.getLogger(AbstractMessageListener::class.java)

    @RabbitListener(queues = ["\${queue.name}"])
    fun receiveRequest(message: Message) {
        message.processInCoroutine(rabbitScope) {
            @Suppress("UNCHECKED_CAST")
            val request = messageConverter.fromMessage(message) as? MessageRequest<T>

            val correlationId = message.messageProperties.correlationId ?: request?.requestId
            val replyTo = message.messageProperties.replyTo
            if (request == null) {
                logger.warn(
                    "Cannot cast to MessageRequest<IMessageAction> for correlationId={}, replyTo={}",
                    correlationId,
                    replyTo
                )
                @Suppress("UNCHECKED_CAST")
                invalidMessageActionHandler.handleInvalidMessageAction(
                    messageConverter.fromMessage(message) as MessageRequest<IMessageAction>,
                    correlationId ?: "",
                    replyTo
                )
                return@processInCoroutine
            }

            var response: MessageResponse? = null
            try {
                val payload = processRequest(request, correlationId ?: "", replyTo)

                response = MessageResponse(
                    requestId = request.requestId,
                    targetId = request.targetId,
                    status = MessageStatus.OK,
                    payload = mapOf("payload" to payload),
                    error = null
                )
            } catch (ex: BadGatewayException) {
                logger.error("BadGatewayException while processing requestId={}: {}", request.requestId, ex.message)
                response = MessageResponse(
                    requestId = request.requestId,
                    targetId = request.targetId,
                    status = MessageStatus.ERROR,
                    error = ex.getErrorProps()
                )
            } catch (ex: NotFoundException) {
                logger.info("NotFoundException while processing requestId={}: {}", request.requestId, ex.message)
                response = MessageResponse(
                    requestId = request.requestId,
                    targetId = request.targetId,
                    status = MessageStatus.NOT_FOUND,
                    error = ex.getErrorProps()
                )
            } catch (ex: Exception) {
                logger.error("Exception while processing requestId={}: {}", request.requestId, ex.message)
                response = MessageResponse(
                    requestId = request.requestId,
                    targetId = request.targetId,
                    status = MessageStatus.ERROR,
                    error = ex.getErrorProps()
                )
            } finally {
                response?.let { messageSender.sendResponse(it, replyTo, correlationId ?: "") }
                    ?: throw NotFoundException("ERROR: response is null for requestId=${request.requestId}")
            }
        }
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveReplyMessage(message: Message) {
        message.processInCoroutine(rabbitScope) {
            logger.debug("ReceiveMessage called, messageProperties: {}", message.messageProperties)
            try {
                val deserializedMessage = messageConverter.fromMessage(message) as MessageResponse
                val correlationId = message.messageProperties.correlationId

                activeMessagingManager.unregisterMessage(correlationId, deserializedMessage)

            } catch (ex: Exception) {
                logger.error("ERROR in receiveMessage: {}", ex.message, ex)
            }
        }
    }

    abstract fun processRequest(request: MessageRequest<T>, correlationId: String, replyTo: String?): Any
}