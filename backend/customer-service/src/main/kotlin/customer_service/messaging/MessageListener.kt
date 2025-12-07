package customer_service.messaging

import com.uhk.fim.prototype.common.messaging.AbstractMessageListener
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.InvalidMessageActionHandler
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.actions.CustomerMessageAction
import customer_service.service.CustomerService
import customer_service.service.SupplierService
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
    private val customerService: CustomerService,
    private val supplierService: SupplierService
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
    ): Any {
        return when (request.action) {
            CustomerMessageAction.GET_CUSTOMER_BY_ID -> {
                val customer = customerService.getCustomerById(request.targetId ?: -1, true)
                customer.toDto()
            }

            CustomerMessageAction.GET_SUPPLIER_BY_ID -> {
                val supplier = supplierService.getSupplierById(request.targetId ?: -1)
                supplier.toDto()
            }
        }
    }
}
