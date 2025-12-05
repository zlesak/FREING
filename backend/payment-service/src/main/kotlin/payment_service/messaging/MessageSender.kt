package payment_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import com.uhk.fim.prototype.common.messaging.sender.RabbitMessageSender
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSender(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitMessageSender: RabbitMessageSender
) {
    val replyQueueName: String = "payment.responses.payment-service-" + UUID.randomUUID().toString()

    init {
        println("[payment-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendTransactionValidationRequest(invoiceId: Long, requestId: String? = null, correlationId: String? = null, payload: Map<String, Any> = emptyMap(), timeoutSeconds: Long = 5, apiSourceService: SourceService = SourceService.PAYMENT): MessageResponse {
        return sendInvoiceRequest(invoiceId, MessageInvoiceAction.VALIDATE_TRANSACTION, requestId, correlationId, payload, timeoutSeconds, apiSourceService)
    }

    fun sendRenderInvoiceRequest(invoiceId: Long, action: MessageInvoiceAction, requestId: String? = null, correlationId: String?= null, payload: Map<String, Any> = emptyMap(), timeoutSeconds: Long = 5, apiSourceService: SourceService = SourceService.PAYMENT): MessageResponse {
        return sendRenderRequest(invoiceId, MessageInvoiceAction.RENDER, requestId, correlationId, payload, timeoutSeconds, apiSourceService)
    }

    private fun sendRenderRequest(invoiceId: Long, action: MessageInvoiceAction, requestId: String? = null, correlationId: String? = null, payload: Map<String, Any> = emptyMap(), timeoutSeconds: Long = 5, apiSourceService: SourceService = SourceService.PAYMENT): MessageResponse {
        return rabbitMessageSender.sendRequest(
            InvoiceRequest(
                apiSourceService = apiSourceService,
                requestId = requestId ?: "",
                targetId = invoiceId,
                action = action,
                payload = payload
            ), RabbitConfig.RENDERING_REQUESTS, replyQueueName, correlationId, timeoutSeconds)
    }

    private fun sendInvoiceRequest(invoiceId: Long, action: MessageInvoiceAction, requestId: String? = null, correlationId: String? = null, payload: Map<String, Any> = emptyMap(), timeoutSeconds: Long = 5, apiSourceService: SourceService = SourceService.PAYMENT): MessageResponse {
        return rabbitMessageSender.sendRequest(
            InvoiceRequest(
                apiSourceService = apiSourceService,
                requestId = requestId ?: "",
                targetId = invoiceId,
                action = action,
                payload = payload
            ), RabbitConfig.INVOICE_REQUESTS, replyQueueName, correlationId, timeoutSeconds)
    }
}

