package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import invoice_service.messaging.MessageSender
import invoice_service.messaging.pendingMessages.PendingCustomerMessages
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Component
class CustomerServiceHandler @Autowired constructor(
    private val messageSender: MessageSender,
    private val pendingCustomerMessages: PendingCustomerMessages
) {
    @Throws(CustomerNotFoundException::class)
    fun getCustomerNameById(
        customerId: Long,
        timeoutSeconds: Long = 5
    ): String {
        val response = sendCustomerRequestAndReturnResponse(customerId, timeoutSeconds)
        if (response.status == "ok") {
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
        timeoutSeconds: Long = 5
    ): Map<String, Any> {
        val response = sendCustomerRequestAndReturnResponse(customerId, timeoutSeconds)
        if (response.status == "ok") {
            @Suppress("UNCHECKED_CAST")
            return response.payload as Map<String, Any>
        } else {
            throw CustomerNotFoundException("Failed to get customer: ${response.error ?: "Unknown error"}")
        }
    }

    fun sendCustomerRequestAndReturnResponse(
        customerId: Long,
        timeoutSeconds: Long = 5
    ): CustomerResponse {
        val future = CompletableFuture<CustomerResponse>()
        sendCustomerRequest(customerId, future)
        return future.get(timeoutSeconds, TimeUnit.SECONDS)
    }

    fun sendCustomerRequest(
        customerId: Long,
        future: CompletableFuture<CustomerResponse>
    ) {
        val correlationId = UUID.randomUUID().toString()
        val requestId = UUID.randomUUID().toString()

        val customerReq = CustomerRequest(
            requestId = requestId,
            customerId = customerId,
            action = "get",
            payload = null
        )
        pendingCustomerMessages.registerCustomerResponseFuture(correlationId, future)
        messageSender.sendCustomerRequest(customerReq, correlationId)
    }
}