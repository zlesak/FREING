package payment_service.messaging

import com.uhk.fim.prototype.common.handlers.GlobalExceptionHandler
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Component
class MessageListener(
    private val messageConverter: MessageConverter,
){
    private val futures = ConcurrentHashMap<String, CompletableFuture<MessageResponse>>()
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    fun registerFuture(): Pair<String, CompletableFuture<MessageResponse>> {
        val correlationId = UUID.randomUUID().toString()
        val future = CompletableFuture<MessageResponse>()
        futures[correlationId] = future
        return correlationId to future
    }

    fun removeFuture(correlationId: String) {
        futures.remove(correlationId)
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveRenderingResponse(message: Message) {
        val response = messageConverter.fromMessage(message) as MessageResponse
        logger.info("Received render response with status: ${response.status} with $message")
        val correlationId = message.messageProperties.correlationId
        if (correlationId != null) {
            futures.remove(correlationId)?.complete(response)
        }
    }
}
