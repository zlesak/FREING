package customer_service.messaging

import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.InvalidMessageActionHandler
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.actions.CustomerMessageAction
import customer_service.dto.customer.response.CustomerDto
import customer_service.service.CustomerService
import com.uhk.fim.prototype.common.messaging.AbstractMessageListener
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
    private val customerService: CustomerService
) : AbstractMessageListener<CustomerMessageAction>(
    messageConverter,
    messageSender,
    invalidMessageActionHandler,
    activeMessagingManager,
    rabbitScope,
    CustomerMessageAction::class.java
) {
    override fun processRequest(
        request: MessageRequest<CustomerMessageAction>,
        correlationId: String,
        replyTo: String?
    ): CustomerDto {
        when (request.action) {
            CustomerMessageAction.GET -> {
                val customer = customerService.getCustomerById(request.targetId ?: -1, true)
                println(customer.email)
                return customer.toDto()
            }
        }
    }
}
