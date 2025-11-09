package customer_service.messaging.handlers

import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import customer_service.messaging.MessageSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class InvalidMessageActionHandler @Autowired constructor(
    private val messageSender: MessageSender
) {
    fun handleInvalidMessageAction(request: CustomerRequest, correlationId: String, replyTo: String) {
        val response = CustomerResponse(
            requestId = request.requestId,
            customerId = request.customerId,
            status = "unsupported_action",
            payload = emptyMap(),
            error = "Unsupported action: ${request.action}"
        )
        messageSender.sendCustomerResponse(response, replyTo, correlationId)
    }
}