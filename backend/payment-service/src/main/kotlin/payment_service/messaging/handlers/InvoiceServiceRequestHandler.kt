package payment_service.messaging.handlers

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.actions.InvoiceMessageAction
import org.springframework.stereotype.Component
import java.util.*

@Component
class InvoiceServiceRequestHandler(
    private val messageSender: MessageSender,
) {
    @Throws(NotFoundException::class)
    fun updateInvoicePaidStatus(
        invoiceId: Long
    ) {
        val response = sendInvoicePaidStatusUpdateRequestAndReturnResponse(invoiceId)
        if (response.status != MessageStatus.OK) {
            throw NotFoundException("Failed to update invoice payed status: ${response.error ?: "Unknown error"}")
        }
    }

    fun sendInvoicePaidStatusUpdateRequestAndReturnResponse(
        invoiceId: Long
    ): MessageResponse {
        return messageSender.sendRequest(
            MessageRequest(
                route = RabbitConfig.INVOICE_REQUESTS,
                requestId = UUID.randomUUID().toString(),
                targetId = invoiceId,
                action = InvoiceMessageAction.PAYED,
                payload = null
            )
        )
    }
}