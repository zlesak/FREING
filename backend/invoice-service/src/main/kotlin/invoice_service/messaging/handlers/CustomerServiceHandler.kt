package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.messaging.ActiveMessagingManager
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.customer.MessageCustomerAction
import invoice_service.messaging.MessageSender
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomerServiceHandler (
    private val messageSender: MessageSender,
) {
    @Throws(NotFoundException::class)
    fun getCustomerNameById(
        customerId: Long,
        apiSourceService: SourceService = SourceService.INVOICE,
        timeoutSeconds: Long = 5
    ): String {
        val response = sendCustomerRequestAndReturnResponse(customerId, apiSourceService, timeoutSeconds)
        if (response.status == MessageStatus.OK) {
            @Suppress("UNCHECKED_CAST")
            val payload = response.payload as Map<String, Any>
            val name = payload["name"] as? String
            val surname = payload["surname"] as? String
            return ("$name $surname")
        } else {
            throw NotFoundException("Failed to get customer: ${response.error ?: "Unknown error"}")
        }
    }
    @Throws(NotFoundException::class)
    fun getCustomerById(
        customerId: Long,
        apiSourceService: SourceService = SourceService.INVOICE,
        timeoutSeconds: Long = 5
    ): Map<String, Any> {
        val response = sendCustomerRequestAndReturnResponse(customerId,apiSourceService, timeoutSeconds)
        if (response.status == MessageStatus.OK) {
            @Suppress("UNCHECKED_CAST")
            return response.payload as Map<String, Any>
        } else {
            throw NotFoundException("Failed to get customer: ${response.error ?: "Unknown error"}")
        }
    }

    fun sendCustomerRequestAndReturnResponse(
        customerId: Long,
        apiSourceService: SourceService = SourceService.INVOICE,
        timeoutSeconds: Long = 5
    ): MessageResponse {
        return messageSender.sendCustomerRequest(customerId, MessageCustomerAction.GET, timeoutSeconds = timeoutSeconds, apiSourceService = apiSourceService)

    }
}