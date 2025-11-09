package invoice_service.messaging.pendingMessages

import com.uhk.fim.prototype.common.exceptions.PendingMessageException
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

@Component
class PendingCustomerMessages {
    private val pendingCustomerResponses = ConcurrentHashMap<String, CompletableFuture<CustomerResponse>>()

    fun registerCustomerResponseFuture(correlationId: String, future: CompletableFuture<CustomerResponse>) {
        pendingCustomerResponses[correlationId] = future
    }
    fun completeCustomerResponseFuture(correlationId: String, response: CustomerResponse) {
        val pending = pendingCustomerResponses.remove(correlationId)
        if (pending != null) {
            pending.complete(response)
            return
        } else {
            throw PendingMessageException("No pending request for correlationId=$correlationId in completeCustomerResponseFuture")
        }
    }
}