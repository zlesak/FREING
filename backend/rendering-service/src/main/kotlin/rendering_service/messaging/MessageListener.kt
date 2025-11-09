package rendering_service.messaging

import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import com.uhk.fim.prototype.common.messaging.dto.RenderingRequest
import com.uhk.fim.prototype.common.messaging.dto.RenderingResponse
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rendering_service.services.PdfRenderingService
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class MessageListener @Autowired constructor(
    private val messageSender: MessageSender,
    private val messageConverter: MessageConverter,
    private val pdfRenderingService: PdfRenderingService
) {
    private val renderingRequests = ConcurrentHashMap<String, Message>()

    @RabbitListener(queues = [RabbitConfig.RENDERING_REQUESTS])
    fun receiveRenderingRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as RenderingRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: return

        println("[rendering-service] Received rendering request: $request, replyTo=$replyTo, correlationId=$correlationId")

        if (request.action == "renderInvoice") {
            renderingRequests[correlationId] = message
            val invoiceRequest = InvoiceRequest(
                requestId = request.requestId,
                invoiceId = request.documentId,
                action = "renderInvoice",
                payload = null
            )
            messageSender.sendInvoiceRequest(invoiceRequest, correlationId)
        } else {
            val render = RenderingResponse(
                requestId = request.requestId,
                documentId = request.documentId,
                status = "error",
                payload = null,
                error = "Unsupported action: ${request.action}"
            )
            messageSender.sendRenderingResponse(render, replyTo, correlationId)
        }
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveInvoiceResponse(message: Message) {
        println("[rendering-service] receiveInvoiceResponse called, messageProperties: ${message.messageProperties}")
        try {
            val response = messageConverter.fromMessage(message) as InvoiceResponse
            val correlationId = message.messageProperties.correlationId ?: response.requestId
            val origRequest = renderingRequests.remove(correlationId) ?: run {
                println("[rendering-service] No original request found for correlationId=$correlationId")
                return
            }
            val origReplyTo = origRequest.messageProperties.replyTo ?: run {
                println("[rendering-service] No replyTo found in message for correlationId=$correlationId")
                return
            }
            println("[rendering-service] Received invoice response: correlationId=$correlationId, replyTo=$origReplyTo")

            if (response.status == "ok" && response.payload != null) {
                @Suppress("UNCHECKED_CAST")
                val invoiceData = response.payload ?: emptyMap()
                val pdfBytes = pdfRenderingService.renderInvoicePdf(invoiceData)
                val pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes)
                val render = RenderingResponse(
                    requestId = response.requestId,
                    documentId = response.invoiceId,
                    status = "ok",
                    payload = mapOf("pdfBase64" to pdfBase64),
                    error = null
                )
                messageSender.sendRenderingResponse(render, origReplyTo, correlationId)
            } else {
                val render = RenderingResponse(
                    requestId = response.requestId,
                    documentId = response.invoiceId,
                    status = "error",
                    payload = null,
                    error = response.error ?: "Invoice data not found"
                )
                messageSender.sendRenderingResponse(render, origReplyTo, correlationId)
            }
        } catch (ex: Exception) {
            println("[rendering-service] ERROR in receiveInvoiceResponse: ${ex.message}")
            ex.printStackTrace()
        }
    }
}
