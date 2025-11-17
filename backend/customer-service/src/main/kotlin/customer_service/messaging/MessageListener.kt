package customer_service.messaging

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.enums.customer.MessageCustomerAction
import customer_service.messaging.handlers.CustomerServiceHandler
import customer_service.messaging.handlers.InvalidMessageActionHandler
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MessageListener @Autowired constructor(
    private val messageConverter: MessageConverter,
    private val invalidMessageActionHandler: InvalidMessageActionHandler,
    private val customerServiceHandler: CustomerServiceHandler
) {

    @RabbitListener(queues = [RabbitConfig.CUSTOMER_REQUESTS])
    fun receiveCustomerRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as CustomerRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: run {
            println("[customer-service] No replyTo in request: $request")
            return
        }

        println("[customer-service] Received customer request: $request, replyTo=$replyTo, correlationId=$correlationId")

        if (request.action == MessageCustomerAction.GET) {
            customerServiceHandler.getCustomer(request, correlationId, replyTo)
        } else {
            invalidMessageActionHandler.handleInvalidMessageAction(request, correlationId, replyTo)
        }
    }
}
