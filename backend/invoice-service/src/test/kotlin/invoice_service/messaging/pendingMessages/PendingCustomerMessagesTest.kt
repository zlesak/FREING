package invoice_service.messaging.pendingMessages

import com.uhk.fim.prototype.common.exceptions.PendingMessageException
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

class PendingCustomerMessagesTest {

    @Test
    fun `register and complete future`() {
        val pending = PendingCustomerMessages()
        val future = CompletableFuture<CustomerResponse>()
        val correlationId = "corr-1"

        pending.register(correlationId, future)

        val response = CustomerResponse(
            requestId = "r1",
            customerId = 1L,
            status = "ok",
            payload = mapOf("name" to "Jan"),
            error = null
        )
        pending.unregister(correlationId, response)

        assertTrue(future.isDone)
        assertEquals(response, future.get())
    }

    @Test
    fun `complete with unknown correlationId throws`() {
        val pending = PendingCustomerMessages()
        val response = CustomerResponse(
            requestId = "r1",
            customerId = 1L,
            status = "ok",
            payload = mapOf("name" to "Jan"),
            error = null
        )
        val unknown = "no-such-id"

        val ex = assertThrows(PendingMessageException::class.java) {
            pending.unregister(unknown, response)
        }
        assertTrue(ex.message!!.contains(unknown))
    }
}

