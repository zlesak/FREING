package payment_service.messaging

import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSender(private val rabbitTemplate: RabbitTemplate) {
    val replyQueueName: String = "payment.responses.payment-service-" + UUID.randomUUID().toString()

    init {
        println("[payment-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendRenderInvoiceRequest(invoiceId: Long, correlationId: String = UUID.randomUUID().toString(), apiSourceService: SourceService = SourceService.PAYMENT) {
        println("[payment-service] Sending render invoice request for invoiceId=$invoiceId with correlationId=$correlationId")
        val request = InvoiceRequest(
            apiSourceService = apiSourceService,
            requestId = UUID.randomUUID().toString(),
            targetId = invoiceId,
            action = MessageInvoiceAction.RENDER,
            payload = null
        )
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

