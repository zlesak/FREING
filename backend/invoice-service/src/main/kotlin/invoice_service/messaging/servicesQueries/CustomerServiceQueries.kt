package invoice_service.messaging.servicesQueries

import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import invoice_service.messaging.MessageListener
import invoice_service.messaging.MessageSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Component
class CustomerServiceQueries @Autowired constructor(
    private val messageSender: MessageSender,
    private val messageListener: MessageListener
) {
    fun getCustomerNameById(customerId: Long, timeoutSeconds: Long = 5): String {
        val correlationId = UUID.randomUUID().toString()
        val requestId = UUID.randomUUID().toString()

        val customerReq = CustomerRequest(
            requestId = requestId,
            customerId = customerId,
            action = "get",
            payload = null
        )

        val future = CompletableFuture<CustomerResponse>()
        messageListener.registerCustomerResponseFuture(correlationId, future)

        messageSender.sendCustomerRequest(customerReq, correlationId)

            val response = future.get(timeoutSeconds, TimeUnit.SECONDS)
            if (response.status == "ok" && response.payload != null) {
                @Suppress("UNCHECKED_CAST")
                val payload = response.payload as Map<String, Any>
                val name = payload["name"] as? String
                val surname = payload["surname"] as? String
                return ("$name $surname")
            }else {
                throw Exception("Failed to get customer: ${response.error ?: "Unknown error"}")
            }
    }
}