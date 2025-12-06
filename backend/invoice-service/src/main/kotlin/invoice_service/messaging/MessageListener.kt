package invoice_service.messaging

import com.uhk.fim.prototype.common.messaging.AbstractMessageListener
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.InvalidMessageActionHandler
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.actions.InvoiceMessageAction
import invoice_service.services.InvoiceService
import invoice_service.services.ZugferdService
import kotlinx.coroutines.CoroutineScope
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class MessageListener(
    messageConverter: MessageConverter,
    messageSender: MessageSender,
    invalidMessageActionHandler: InvalidMessageActionHandler,
    activeMessagingManager: ActiveMessagingManager,
    rabbitScope: CoroutineScope,
    private val zugferdService: ZugferdService,
    private val invoiceService: InvoiceService,
) : AbstractMessageListener<InvoiceMessageAction>(
    messageConverter,
    messageSender,
    invalidMessageActionHandler,
    activeMessagingManager,
    rabbitScope,
    InvoiceMessageAction::class.java
) {
    override fun processRequest(
        request: MessageRequest<InvoiceMessageAction>,
        correlationId: String,
        replyTo: String?
    ): String {
        return when (request.action) {
            InvoiceMessageAction.GET -> {
                zugferdService.createInvoice(request.targetId ?: -1)
            }
            InvoiceMessageAction.PAYED -> {
                invoiceService.markInvoiceAsPayed(request.targetId ?: -1).toString()
            }
            InvoiceMessageAction.READ -> {
                invoiceService.markInvoiceAsRead(request.targetId ?: -1).toString()
            }
        }
    }
}