package rendering_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
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
    private val rabbitMessageSender: RabbitMessageSender,
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
        return rabbitMessageSender.sendRequest(InvoiceRequest(
            apiSourceService = apiSourceService,
            requestId = requestId?:"",
            targetId = targetId,
            action = action,
            payload = payload
        ), RabbitConfig.INVOICE_REQUESTS, replyQueueName, correlationId, timeout)
    }

    fun sendRenderingResponse(response: MessageResponse, replyTo: String, correlationId: String) {
        println("[rendering-service] Sending rendering response to $replyTo with correlationId=$correlationId")
        return rabbitMessageSender.sendResponse(response, replyTo, correlationId)
    }
}
