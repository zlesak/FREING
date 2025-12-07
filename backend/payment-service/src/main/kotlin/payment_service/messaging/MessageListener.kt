package payment_service.messaging

import com.uhk.fim.prototype.common.messaging.AbstractMessageListener
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.InvalidMessageActionHandler
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.actions.PaymentMessageAction
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
) : AbstractMessageListener<PaymentMessageAction>(
    messageConverter,
    messageSender,
    invalidMessageActionHandler,
    activeMessagingManager,
    rabbitScope,
    PaymentMessageAction::class.java
) {
    override fun processRequest(
        request: MessageRequest<PaymentMessageAction>,
        correlationId: String,
        replyTo: String?
    ): Any {
        throw NotImplementedError() //NOT IMPLEMENTED NOT NEEDED NOW
    }
}
