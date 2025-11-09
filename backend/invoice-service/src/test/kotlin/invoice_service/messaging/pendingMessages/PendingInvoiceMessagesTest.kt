package invoice_service.messaging.pendingMessages

import com.uhk.fim.prototype.common.exceptions.PendingMessageException
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

class PendingInvoiceMessagesTest {

    @Test
    fun `register and complete future`() {
        val pending = PendingInvoiceMessages()
        val future = CompletableFuture<InvoiceResponse>()
        val correlationId = "corr-1"

        pending.registerInvoiceResponseFuture(correlationId, future)

        val response = InvoiceResponse(
            requestId = "r1",
            invoiceId = 1L,
            status = "ok",
            payload = mapOf("xml" to "<xml/>"),
            error = null
        )
        pending.completeInvoiceResponseFuture(correlationId, response)

        assertTrue(future.isDone)
        assertEquals(response, future.get())
    }

    @Test
    fun `complete with unknown correlationId throws`() {
        val pending = PendingInvoiceMessages()
        val response = InvoiceResponse(
            requestId = "r1",
            invoiceId = 1L,
            status = "ok",
            payload = mapOf("xml" to "<xml/>"),
            error = null
        )
        val unknown = "no-such-id"

        val ex = assertThrows(PendingMessageException::class.java) {
            pending.completeInvoiceResponseFuture(unknown, response)
        }
        assertTrue(ex.message!!.contains(unknown))
    }
}

