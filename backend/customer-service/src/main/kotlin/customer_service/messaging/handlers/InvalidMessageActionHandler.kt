package customer_service.messaging.handlers

import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.ErrorProps
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import customer_service.messaging.MessageSender
import org.springframework.stereotype.Component

@Component
class InvalidMessageActionHandler (
    private val messageSender: MessageSender
) {
    fun handleInvalidMessageAction(request: CustomerRequest, correlationId: String, replyTo: String, apiSourceService: SourceService = SourceService.CUSTOMER) {
        val response = MessageResponse(
            apiSourceService = apiSourceService,
            sourceService = SourceService.CUSTOMER,
            requestId = request.requestId,
            targetId = request.targetId,
            status = MessageStatus.UNSUPPORTED_ACTION,
            payload = emptyMap(),
            error = ErrorProps(
                "Unsupported action: ${request.action}",
                IllegalStateException::class.java
            )
        )
        messageSender.sendCustomerResponse(response, replyTo, correlationId)
    }
}