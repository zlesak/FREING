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
    fun receiveReplyMessage(message: Message) {
        println("[invoice-service] receiveMessage called, messageProperties: ${message.messageProperties}")
        try {
            val deserializedMessage = messageConverter.fromMessage(message)
            val correlationId = message.messageProperties.correlationId ?: when (deserializedMessage) {
                is CustomerResponse -> deserializedMessage.requestId
                is InvoiceResponse -> deserializedMessage.requestId
                else -> throw IllegalArgumentException("Unknown message type")
            }

            when (deserializedMessage) {
                is CustomerResponse -> pendingCustomerResponses.completeCustomerResponseFuture(correlationId, deserializedMessage)
                is InvoiceResponse -> pendingInvoiceResponses.completeInvoiceResponseFuture(correlationId, deserializedMessage)
                else -> throw IllegalArgumentException("Unsupported message type: ${deserializedMessage::class}")
            }
        } catch (ex: Exception) {
            println("[invoice-service] ERROR in receiveMessage: ${ex.message}")
            ex.printStackTrace()
        }
    }
}