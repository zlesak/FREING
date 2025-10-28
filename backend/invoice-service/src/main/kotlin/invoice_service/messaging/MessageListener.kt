package invoice_service.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.services.InvoiceService
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MessageListener @Autowired constructor(
    private val messageSender: MessageSender,
    private val messageConverter: MessageConverter,
    private val invoiceService: InvoiceService,
    private val objectMapper: ObjectMapper
) {
    @RabbitListener(queues = [RabbitConfig.INVOICE_REQUESTS])
    fun receiveInvoiceRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as InvoiceRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: return

        println("[invoice-service] Received invoice request: $request, replyTo=$replyTo, correlationId=$correlationId")

        if (request.action == "getById") {
            val invoice = invoiceService.getInvoice(request.invoiceId ?: -1)
            val invoiceData = invoice?.toMap(objectMapper)
            if (invoiceData != null) {
                val response = InvoiceResponse(
                    requestId = request.requestId,
                    invoiceId = request.invoiceId,
                    status = "ok",
                    payload = invoiceData,
                    error = null
                )
                messageSender.sendInvoiceResponse(response, replyTo, correlationId)
            } else {
                val response = InvoiceResponse(
                    requestId = request.requestId,
                    invoiceId = request.invoiceId,
                    status = "not_found",
                    payload = null,
                    error = "Invoice not found"
                )
                messageSender.sendInvoiceResponse(response, replyTo, correlationId)
            }
        } else {
            val response = InvoiceResponse(
                requestId = request.requestId,
                invoiceId = request.invoiceId,
                status = "unsupported_action",
                payload = null,
                error = "Unsupported action: ${request.action}"
            )
            messageSender.sendInvoiceResponse(response, replyTo, correlationId)
        }
        println("[invoice-service] End $request, replyTo=$replyTo, correlationId=$correlationId")
    }
}
