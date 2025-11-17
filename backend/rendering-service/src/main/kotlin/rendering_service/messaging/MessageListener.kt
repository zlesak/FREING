package rendering_service.messaging

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component
import rendering_service.services.PdfRenderingService
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class MessageListener (
    private val messageSender: MessageSender,
    private val messageConverter: MessageConverter,
    private val pdfRenderingService: PdfRenderingService,
    private val activeMessagingManager: ActiveMessagingManager,
) {
    private val renderingRequests = ConcurrentHashMap<String, Message>()

    @RabbitListener(queues = [RabbitConfig.RENDERING_REQUESTS])
    fun receiveRenderingRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as InvoiceRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: return

        println("[rendering-service] Received rendering request: $request, replyTo=$replyTo, correlationId=$correlationId")

        if (request.action == MessageInvoiceAction.RENDER) {
            renderingRequests[correlationId] = message
            messageSender.sendInvoiceRequest(
                targetId = request.targetId,
                requestId = request.requestId,
                correlationId = correlationId,
                action = MessageInvoiceAction.RENDER,
                apiSourceService = request.apiSourceService
                )
        } else {
            val render = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.RENDER,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = "Unsupported action: ${request.action}"
            )
            messageSender.sendRenderingResponse(render, replyTo, correlationId)
        }
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveInvoiceResponse(message: Message) {
        println("[rendering-service] receiveInvoiceResponse called, messageProperties: ${message.messageProperties}")
        try {
            val response = messageConverter.fromMessage(message) as MessageResponse
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

            if (response.status == MessageStatus.OK) {
                @Suppress("UNCHECKED_CAST")
                val invoiceData = response.payload
                val pdfBytes = pdfRenderingService.renderInvoicePdf(invoiceData)
                val pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes)
                val render = MessageResponse(
                    apiSourceService = response.apiSourceService,
                    sourceService = SourceService.INVOICE,
                    requestId = response.requestId,
                    targetId = response.targetId,
                    status = MessageStatus.OK,
                    payload = mapOf("pdfBase64" to pdfBase64),
                    error = null
                )
                messageSender.sendRenderingResponse(render, origReplyTo, correlationId)
            } else {
                val render = MessageResponse(
                    apiSourceService = response.apiSourceService,
                    sourceService = SourceService.INVOICE,
                    requestId = response.requestId,
                    targetId = response.targetId,
                    status = MessageStatus.ERROR,
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
