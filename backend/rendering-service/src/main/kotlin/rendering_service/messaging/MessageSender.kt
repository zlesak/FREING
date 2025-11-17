package rendering_service.messaging

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSender(
    private val rabbitTemplate: RabbitTemplate,
    private val activeMessagingManager: ActiveMessagingManager,
) {
    final val replyQueueName: String = "rendering.responses.rendering-service-" + UUID.randomUUID().toString()

    init {
        println("[rendering-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }


    fun sendInvoiceRequest(targetId: Long?, action: MessageInvoiceAction, requestId: String? = null, correlationId: String ?=null, timeout: Long = 5, payload: Map<String, Any> = emptyMap(), apiSourceService: SourceService = SourceService.PAYMENT): MessageResponse {
        return activeMessagingManager.registerMessage(timeout, correlationId = correlationId, requestId = requestId) { messageIds ->
            val request = InvoiceRequest(
                apiSourceService = apiSourceService,
                requestId = messageIds.requestId,
                targetId = targetId,
                action = action,
                payload = payload
            )
            sendInvoiceRequest(request, messageIds.correlationId)
        }
    }

    private fun sendInvoiceRequest(request: InvoiceRequest, correlationId: String) {
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

    fun sendRenderingResponse(response: MessageResponse, replyTo: String, correlationId: String) {
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
