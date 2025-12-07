package rendering_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.AbstractMessageListener
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.InvalidMessageActionHandler
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.actions.InvoiceMessageAction
import com.uhk.fim.prototype.common.messaging.enums.actions.RenderMessageAction
import kotlinx.coroutines.CoroutineScope
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component
import rendering_service.services.PdfRenderingService
import java.util.*

@Component
class MessageListener(
    messageConverter: MessageConverter,
    private val messageSender: MessageSender,
    invalidMessageActionHandler: InvalidMessageActionHandler,
    activeMessagingManager: ActiveMessagingManager,
    rabbitScope: CoroutineScope,
    private val pdfRenderingService: PdfRenderingService,
) : AbstractMessageListener<RenderMessageAction>(
    messageConverter,
    messageSender,
    invalidMessageActionHandler,
    activeMessagingManager,
    rabbitScope,
    RenderMessageAction::class.java
) {
    override fun processRequest(
        request: MessageRequest<RenderMessageAction>,
        correlationId: String,
        replyTo: String?
    ): Any {

        this.messageSender

        when (request.action) {
            RenderMessageAction.RENDER -> {
                val invoiceData = messageSender.sendRequest(
                    MessageRequest(
                        route = RabbitConfig.INVOICE_REQUESTS,
                        requestId = UUID.randomUUID().toString(),
                        targetId = request.targetId ?: -1,
                        action = InvoiceMessageAction.GET,
                        payload = null
                    )
                ).payload
                val xml = invoiceData["payload"] as? String
                    ?: throw IllegalArgumentException("Missing XML data in payload")
                val pdfBytes = pdfRenderingService.renderInvoicePdf(xml)
                return Base64.getEncoder().encodeToString(pdfBytes)
            }

            RenderMessageAction.RENDER_REPORT -> {
                val payload = request.payload as? Map<String, Any?>
                    ?: throw IllegalArgumentException("Missing or invalid payload for report rendering")
                val xml = payload["payload"] as? String
                    ?: throw IllegalArgumentException("Missing XML data in payload")
                val pdfBytes = pdfRenderingService.renderReportPdf(xml)
                return Base64.getEncoder().encodeToString(pdfBytes)
            }
        }
    }
}
