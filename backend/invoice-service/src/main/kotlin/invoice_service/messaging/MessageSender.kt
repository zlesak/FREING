package invoice_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.customer.MessageCustomerAction
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
    val replyQueueName: String = "invoice.responses.invoice-service-" + UUID.randomUUID().toString()

    init {
        println("[invoice-service] replyQueueName = $replyQueueName")
        rabbitTemplate.execute { channel ->
            channel.queueDeclare(replyQueueName, true, false, false, null)
            null
        }
    }

    fun sendCustomerRequest(customerId: Long, action: MessageCustomerAction, requestId: String? = null, correlationId: String?= null, payload: Map<String, Any>? = emptyMap(), timeoutSeconds: Long = 5, apiSourceService: SourceService = SourceService.INVOICE): MessageResponse {
        return rabbitMessageSender.sendRequest(CustomerRequest(
            apiSourceService,
            requestId?:"",
            customerId,
            action,
            payload
        ),  RabbitConfig.CUSTOMER_REQUESTS, replyQueueName, correlationId, timeoutSeconds)
    }

    fun sendInvoiceRequest(invoiceId: Long, action: MessageInvoiceAction, requestId: String? = null, correlationId: String?= null, payload: Map<String, Any> = emptyMap(), timeoutSeconds: Long = 5,  apiSourceService: SourceService = SourceService.INVOICE): MessageResponse {
       return rabbitMessageSender.sendRequest(InvoiceRequest(
           apiSourceService,
           requestId?:"",
           invoiceId,
           action,
           payload
       ), RabbitConfig.INVOICE_REQUESTS, replyQueueName, correlationId, timeoutSeconds)
    }

    fun sendInvoiceResponse(response: MessageResponse, replyTo: String, correlationId: String) {
        println("[invoice-service] Sending invoice response to $replyTo with correlationId=$correlationId")
        rabbitMessageSender.sendResponse(response, replyTo, correlationId)
    }
}
