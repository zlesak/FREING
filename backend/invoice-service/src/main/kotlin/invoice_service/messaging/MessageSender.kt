package invoice_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.customer.MessageCustomerAction
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSender(
    private val rabbitTemplate: RabbitTemplate,
    private val activeMessagingManager: ActiveMessagingManager
) {
    val replyQueueName: String = "invoice.responses.invoice-service-" + UUID.randomUUID().toString()

    init {
        println("[invoice-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendCustomerRequest(customerId: Long, action: MessageCustomerAction, requestId: String? = null, correlationId: String?= null, payload: Map<String, Any>? = emptyMap(), timeoutSeconds: Long = 5, apiSourceService: SourceService = SourceService.INVOICE): MessageResponse {
       return activeMessagingManager.registerMessage(timeoutSeconds = timeoutSeconds, correlationId = correlationId, requestId = requestId) { messageIds ->
            val request = CustomerRequest(
                apiSourceService,
                messageIds.requestId,
                customerId,
                action,
                payload
            )

           println("[invoice-service] Sending customer requestId: ${messageIds.requestId} with correlationId=${messageIds.correlationId}")
            sendCustomerRequest(request, messageIds.correlationId)
        }
    }

    private fun sendCustomerRequest(request: CustomerRequest, correlationId: String){
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

    fun sendInvoiceResponse(response: MessageResponse, replyTo: String, correlationId: String) {
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

    fun sendInvoiceRequest(invoiceId: Long, action: MessageInvoiceAction, requestId: String? = null, correlationId: String?= null, payload: Map<String, Any> = emptyMap(), timeoutSeconds: Long = 5,  apiSourceService: SourceService = SourceService.INVOICE): MessageResponse {
        return activeMessagingManager.registerMessage(timeoutSeconds = timeoutSeconds, requestId = requestId, correlationId = correlationId) { messageIds ->
            val request = InvoiceRequest(
                apiSourceService,
                messageIds.requestId,
                invoiceId,
                action,
                payload
            )
            println("[invoice-service] Sending invoice requestId: ${messageIds.requestId} with correlationId=${messageIds.correlationId}")
            sendInvoiceRequest(request, messageIds.correlationId)
        }
    }

    private fun sendInvoiceRequest(request: InvoiceRequest, correlationId: String) {
        println("[invoice-service] Sending invoice request with correlationId=$correlationId")
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.INVOICE_REQUESTS,
            request
        ) { message ->
            message.messageProperties.correlationId = correlationId
            message.messageProperties.replyTo = replyQueueName
            message
        }
    }
}
