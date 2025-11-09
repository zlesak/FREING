package invoice_service.messaging.pendingMessages

import com.uhk.fim.prototype.common.exceptions.PendingMessageException
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Component
class PendingInvoiceMessages {

    private val pendingInvoiceResponses = ConcurrentHashMap<String, CompletableFuture<InvoiceResponse>>()
    fun registerInvoiceResponseFuture(correlationId: String, future: CompletableFuture<InvoiceResponse>) {
        pendingInvoiceResponses[correlationId] = future
    }

    fun completeInvoiceResponseFuture(correlationId: String, response: InvoiceResponse) {
        val pending = pendingInvoiceResponses.remove(correlationId)
        if (pending != null) {
            pending.complete(response)
            return
        } else {
            throw PendingMessageException("No pending request for correlationId=$correlationId in completeCustomerResponseFuture")
        }
    }
}