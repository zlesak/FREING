package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import invoice_service.messaging.MessageSender
import invoice_service.messaging.pendingMessages.PendingCustomerMessages
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.concurrent.CompletableFuture

class CustomerServiceHandlerTest {

    @Test
    fun `sendCustomerRequest registers future and delegates to messageSender`() {
        val messageSender: MessageSender = mock()
        val pending: PendingCustomerMessages = mock()

        val handler = CustomerServiceHandler(messageSender, pending)

        val future = CompletableFuture<CustomerResponse>()

        handler.sendCustomerRequest(42L, future)

        val corrCaptor = argumentCaptor<String>()
        val futCaptor = argumentCaptor<CompletableFuture<CustomerResponse>>()

        verify(pending, times(1)).registerCustomerResponseFuture(corrCaptor.capture(), futCaptor.capture())
        val capturedCorr = corrCaptor.firstValue
        assertNotNull(capturedCorr)

        verify(messageSender, times(1)).sendCustomerRequest(any(), eq(capturedCorr))
    }

    @Test
    fun `getCustomerNameById returns concatenated name when status ok`() {
        val messageSender: MessageSender = mock()
        val pending: PendingCustomerMessages = mock()

        val handler = CustomerServiceHandler(messageSender, pending)

        whenever(pending.registerCustomerResponseFuture(any(), any())).thenAnswer { inv ->
            val fut = inv.getArgument<CompletableFuture<CustomerResponse>>(1)
            val resp = CustomerResponse(
                requestId = "r1",
                customerId = 42L,
                status = "ok",
                payload = mapOf("name" to "John", "surname" to "Doe"),
                error = null
            )
            fut.complete(resp)
            null
        }

        val name = handler.getCustomerNameById(42L, 1)
        assertEquals("John Doe", name)
    }

    @Test
    fun `getCustomerById throws when status not ok`() {
        val messageSender: MessageSender = mock()
        val pending: PendingCustomerMessages = mock()

        val handler = CustomerServiceHandler(messageSender, pending)

        whenever(pending.registerCustomerResponseFuture(any(), any())).thenAnswer { inv ->
            val fut = inv.getArgument<CompletableFuture<CustomerResponse>>(1)
            val resp = CustomerResponse(
                requestId = "r1",
                customerId = 42L,
                status = "error",
                payload = emptyMap(),
                error = "Not found"
            )
            fut.complete(resp)
            null
        }

        assertThrows(CustomerNotFoundException::class.java) {
            handler.getCustomerById(42L, 1)
        }
    }
}

