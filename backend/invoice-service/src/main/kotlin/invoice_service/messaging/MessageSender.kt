package invoice_service.messaging

import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.RabbitConfig
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSender(private val rabbitTemplate: RabbitTemplate) {
    val replyQueueName: String = "invoice.responses.invoice-service-" + UUID.randomUUID().toString()

    init {
        println("[invoice-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendCustomerRequest(request: CustomerRequest, correlationId: String = UUID.randomUUID().toString()) {
        println("[invoice-service] Sending customer request: $request with correlationId=$correlationId")
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.CUSTOMER_REQUESTS,
            request
        ) { message ->
            message.messageProperties.replyTo = replyQueueName
            message.messageProperties.correlationId = correlationId
            message
        }
    }

    fun sendInvoiceResponse(response: InvoiceResponse, replyTo: String, correlationId: String) {
        println("[invoice-service] Sending invoice response to $replyTo with correlationId=$correlationId")
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
