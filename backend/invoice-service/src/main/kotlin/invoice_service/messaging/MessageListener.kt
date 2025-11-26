package invoice_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.extensions.processInCoroutine
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import invoice_service.messaging.handlers.InvalidMessageActionHandler
import invoice_service.messaging.handlers.InvoiceServiceHandler
import kotlinx.coroutines.CoroutineScope
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class MessageListener (
    private val messageConverter: MessageConverter,
    private val invoiceServiceHandler: InvoiceServiceHandler,
    private val invalidMessageActionHandler: InvalidMessageActionHandler,
    private val activeMessagingManager: ActiveMessagingManager,
    private val rabbitScope: CoroutineScope,
) {

    @RabbitListener(queues = [RabbitConfig.INVOICE_REQUESTS])
    fun receiveInvoiceRequest(message: Message) {
        message.processInCoroutine(rabbitScope){
            processInvoiceRequest(message)
        }
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveReplyMessage(message: Message) {
       message.processInCoroutine(rabbitScope){
           processReply(message)
       }
    }

    suspend fun processInvoiceRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as InvoiceRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: return

        println("[invoice-service] Received invoice request: $request, replyTo=$replyTo, correlationId=$correlationId")

        if (request.action == MessageInvoiceAction.RENDER) {
            invoiceServiceHandler.createXmlInvoice(request, correlationId, replyTo)
        } else {
            invalidMessageActionHandler.handleInvalidMessageAction(request, correlationId, replyTo)
        }
        println("[invoice-service] End $request, replyTo=$replyTo, correlationId=$correlationId")
    }

    suspend fun processReply(message: Message) {
        println("[invoice-service] receiveMessage called, messageProperties: ${message.messageProperties}")
        try {
            val deserializedMessage = messageConverter.fromMessage(message) as MessageResponse
            val correlationId = message.messageProperties.correlationId ?: when (deserializedMessage.sourceService) {
                SourceService.CUSTOMER, SourceService.INVOICE -> deserializedMessage.requestId
                else -> throw IllegalArgumentException("Unknown message type")
            }

            when (deserializedMessage.sourceService) {
                SourceService.CUSTOMER, SourceService.INVOICE-> activeMessagingManager.unregisterMessage(correlationId, deserializedMessage)
                else -> throw IllegalArgumentException("Unsupported message type: ${deserializedMessage::class}")
            }
        } catch (ex: Exception) {
            println("[invoice-service] ERROR in receiveMessage: ${ex.message}")
            ex.printStackTrace()
        }
    }
}