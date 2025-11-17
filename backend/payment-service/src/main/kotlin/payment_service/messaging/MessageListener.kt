package payment_service.messaging

import com.uhk.fim.prototype.common.handlers.GlobalExceptionHandler
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class MessageListener(
    private val messageConverter: MessageConverter,
    protected val activeMessagingManager: ActiveMessagingManager
){
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveRenderingResponse(message: Message) {
        logger.info("Received render response with status:")
        val response = messageConverter.fromMessage(message) as MessageResponse
        logger.info("Received render response with status: ${response.status} with $message")
        val correlationId = message.messageProperties.correlationId
        if (correlationId != null) {
           activeMessagingManager.unregisterMessage(correlationId, response)
        }
    }
}
