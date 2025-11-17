package payment_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
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
    private val activeMessagingManager: ActiveMessagingManager) {
    val replyQueueName: String = "payment.responses.payment-service-" + UUID.randomUUID().toString()

    init {
        println("[payment-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendRenderInvoiceRequest(invoiceId: Long, action: MessageInvoiceAction, requestId: String? = null, correlationId: String?= null, payload: Map<String, Any> = emptyMap(), timeoutSeconds: Long = 5, apiSourceService: SourceService = SourceService.PAYMENT): MessageResponse {
        return activeMessagingManager.registerMessage(timeoutSeconds = timeoutSeconds, requestId = requestId, correlationId = correlationId) { messageIds ->
            val request = InvoiceRequest(
            apiSourceService = apiSourceService,
            requestId = messageIds.requestId,
            targetId = invoiceId,
            action = action,
            payload = payload
            )
            sendRenderInvoiceRequest(request, messageIds.correlationId)
        }
    }

    private fun sendRenderInvoiceRequest(request: InvoiceRequest, correlationId: String) {
        println("[payment-service] Sending render invoice request for invoiceId=${request.targetId} with correlationId=$correlationId")
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.RENDERING_REQUESTS,
            request
        ) { message ->
            message.messageProperties.replyTo = replyQueueName
            message.messageProperties.correlationId = correlationId
            message
        }
    }

}

