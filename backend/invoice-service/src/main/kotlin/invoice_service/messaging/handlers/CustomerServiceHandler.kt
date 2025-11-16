package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.customer.MessageCustomerAction
import invoice_service.messaging.MessageSender
import invoice_service.messaging.pendingMessages.PendingCustomerMessages
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Component
class CustomerServiceHandler (
    private val messageSender: MessageSender,
    private val pendingCustomerMessages: PendingCustomerMessages
) {
    @Throws(CustomerNotFoundException::class)
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
            throw CustomerNotFoundException("Failed to get customer: ${response.error ?: "Unknown error"}")
        }
    }
    @Throws(CustomerNotFoundException::class)
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
            throw CustomerNotFoundException("Failed to get customer: ${response.error ?: "Unknown error"}")
        }
    }

    fun sendCustomerRequestAndReturnResponse(
        customerId: Long,
        apiSourceService: SourceService = SourceService.INVOICE,
        timeoutSeconds: Long = 5
    ): MessageResponse {
        val future = CompletableFuture<MessageResponse>()
        sendCustomerRequest(customerId, apiSourceService, future)
        return future.get(timeoutSeconds, TimeUnit.SECONDS)
    }

    fun sendCustomerRequest(
        customerId: Long,
        apiSourceService: SourceService = SourceService.INVOICE,
        future: CompletableFuture<MessageResponse>
    ) {
        val correlationId = UUID.randomUUID().toString()
        val requestId = UUID.randomUUID().toString()

        val customerReq = CustomerRequest(
            apiSourceService = apiSourceService,
            requestId = requestId,
            targetId = customerId,
            action = MessageCustomerAction.GET,
            payload = null
        )
        pendingCustomerMessages.registerCustomerResponseFuture(correlationId, future)
        messageSender.sendCustomerRequest(customerReq, correlationId)
    }
}