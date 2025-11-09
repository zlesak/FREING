package invoice_service.messaging

import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.messaging.pendingMessages.PendingCustomerMessages
import invoice_service.messaging.pendingMessages.PendingInvoiceMessages
import invoice_service.messaging.handlers.InvalidMessageActionHandler
import invoice_service.messaging.handlers.InvoiceServiceHandler
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MessageListener @Autowired constructor(
    private val messageConverter: MessageConverter,
    private val invoiceServiceHandler: InvoiceServiceHandler,
    private val invalidMessageActionHandler: InvalidMessageActionHandler,
    private val pendingCustomerResponses: PendingCustomerMessages,
    private val pendingInvoiceResponses: PendingInvoiceMessages
) {

    @RabbitListener(queues = [RabbitConfig.INVOICE_REQUESTS])
    fun receiveInvoiceRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as InvoiceRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: return

        println("[invoice-service] Received invoice request: $request, replyTo=$replyTo, correlationId=$correlationId")

        if (request.action == "renderInvoice") {
            invoiceServiceHandler.createXmlInvoice(request, correlationId, replyTo)
        } else {
            invalidMessageActionHandler.handleInvalidMessageAction(request, correlationId, replyTo)
        }
        println("[invoice-service] End $request, replyTo=$replyTo, correlationId=$correlationId")
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveCustomerResponse(message: Message) {
        println("[invoice-service] receiveCustomerResponse called, messageProperties: ${message.messageProperties}")
        try {
            val response = messageConverter.fromMessage(message) as CustomerResponse
            val correlationId = message.messageProperties.correlationId ?: response.requestId

            pendingCustomerResponses.completeCustomerResponseFuture(correlationId, response)
        } catch (ex: Exception) {
            println("[invoice-service] ERROR in receiveCustomerResponse: ${ex.message}")
            ex.printStackTrace()
        }
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveInvoiceResponse(message: Message) {
        try {
            val response = messageConverter.fromMessage(message) as InvoiceResponse
            val correlationId = message.messageProperties.correlationId ?: response.requestId

            pendingInvoiceResponses.completeInvoiceResponseFuture(correlationId, response)
        } catch (ex: Exception) {
            println("[invoice-service] ERROR in receiveInvoiceResponse: ${ex.message}")
            ex.printStackTrace()
        }
    }
}