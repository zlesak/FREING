package invoice_service.messaging.servicesHandlers

import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.messaging.MessageSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class InvalidMessageActionHandler @Autowired constructor(
    private val messageSender: MessageSender
) {
    fun handleInvalidMessageAction(request: InvoiceRequest, correlationId: String, replyTo: String) {
         val response = InvoiceResponse(
             requestId = request.requestId,
             invoiceId = request.invoiceId,
             status = "unsupported_action",
             payload = null,
             error = "Unsupported action: ${request.action}"
         )
        messageSender.sendInvoiceResponse(response, replyTo, correlationId)
    }
}