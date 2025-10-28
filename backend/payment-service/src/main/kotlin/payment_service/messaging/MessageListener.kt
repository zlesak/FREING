package payment_service.messaging

import com.uhk.fim.prototype.common.messaging.dto.RenderingResponse
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Component
class MessageListener @Autowired constructor(
    private val messageConverter: MessageConverter,
){
    private val futures = ConcurrentHashMap<String, CompletableFuture<RenderingResponse>>()

    fun registerFuture(): Pair<String, CompletableFuture<RenderingResponse>> {
        val correlationId = UUID.randomUUID().toString()
        val future = CompletableFuture<RenderingResponse>()
        futures[correlationId] = future
        return correlationId to future
    }

    fun removeFuture(correlationId: String) {
        futures.remove(correlationId)
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveRenderingResponse(message: Message) {
        val response = messageConverter.fromMessage(message) as RenderingResponse
        val correlationId = message.messageProperties.correlationId
        if (correlationId != null) {
            futures.remove(correlationId)?.complete(response)
        }
    }
}
