package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.messaging.dto.ErrorProps
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import invoice_service.messaging.MessageSender
import org.springframework.stereotype.Component

@Component
class InvalidMessageActionHandler (
    private val messageSender: MessageSender
) {
    fun handleInvalidMessageAction(request: InvoiceRequest, correlationId: String, replyTo: String, apiSourceService: SourceService = SourceService.INVOICE) {
         val response = MessageResponse(
             apiSourceService = apiSourceService,
             sourceService = SourceService.INVOICE,
             requestId = request.requestId,
             targetId = request.targetId,
             status = MessageStatus.UNSUPPORTED_ACTION,
             error = ErrorProps(
                 "Unsupported action: ${request.action}",
                 IllegalStateException::class.java
             )
         )
        messageSender.sendInvoiceResponse(response, replyTo, correlationId)
    }
}