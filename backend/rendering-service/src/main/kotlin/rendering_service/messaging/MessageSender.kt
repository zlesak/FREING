package rendering_service.messaging

import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.RenderingResponse
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSender(private val rabbitTemplate: RabbitTemplate) {
    val replyQueueName: String = "rendering.responses.rendering-service-" + UUID.randomUUID().toString()

    init {
        println("[rendering-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendInvoiceRequest(request: InvoiceRequest, correlationId: String = UUID.randomUUID().toString()) {
        println("[rendering-service] Sending invoice request: $request with correlationId=$correlationId")
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.INVOICE_REQUESTS,
            request
        ) { message ->
            message.messageProperties.replyTo = replyQueueName
            message.messageProperties.correlationId = correlationId
            message
        }
    }

    fun sendRenderingResponse(response: RenderingResponse, replyTo: String, correlationId: String) {
        println("[rendering-service] Sending rendering response to $replyTo with correlationId=$correlationId")
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
