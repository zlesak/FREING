package invoice_service.messaging

import com.uhk.fim.prototype.common.messaging.AbstractMessageListener
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.InvalidMessageActionHandler
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.actions.InvoiceMessageAction
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
        when (request.action) {
            InvoiceMessageAction.GET -> {
                return zugferdService.createInvoice(request.targetId ?: -1)
            }
        }
    }
}