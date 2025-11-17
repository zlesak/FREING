package invoice_service.messaging

import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.messaging.handlers.InvalidMessageActionHandler
import invoice_service.messaging.handlers.InvoiceServiceHandler
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.support.converter.MessageConverter

class MessageListenerTest {

    @Test
    fun `receiveCustomerResponse completes pending future`() {
        val converter: MessageConverter = mock()
        val invoiceHandler: InvoiceServiceHandler = mock()
        val invalidHandler: InvalidMessageActionHandler = mock()
        val pendingCustomer = spy(PendingCustomerMessages())
        val pendingInvoice = spy(PendingInvoiceMessages())

        val listener = MessageListener(converter, invoiceHandler, invalidHandler, pendingCustomer, pendingInvoice)

        val resp = CustomerResponse(
            requestId = "r1",
            customerId = 2L,
            status = "ok",
            payload = mapOf("name" to "A"),
            error = null
        )
        val props = MessageProperties()
        props.correlationId = "corr-1"
        val message = Message(ByteArray(0), props)

        whenever(converter.fromMessage(message)).thenReturn(resp)

        listener.receiveCustomerResponse(message)

        verify(pendingCustomer, times(1)).unregister("corr-1", resp)
    }

    @Test
    fun `receiveInvoiceResponse completes pending future`() {
        val converter: MessageConverter = mock()
        val invoiceHandler: InvoiceServiceHandler = mock()
        val invalidHandler: InvalidMessageActionHandler = mock()
        val pendingCustomer = spy(PendingCustomerMessages())
        val pendingInvoice = spy(PendingInvoiceMessages())

        val listener = MessageListener(converter, invoiceHandler, invalidHandler, pendingCustomer, pendingInvoice)

        val resp = InvoiceResponse(
            requestId = "r1",
            invoiceId = 2L,
            status = "ok",
            payload = mapOf("xml" to "x"),
            error = null
        )
        val props = MessageProperties()
        props.correlationId = "corr-2"
        val message = Message(ByteArray(0), props)

        whenever(converter.fromMessage(message)).thenReturn(resp)

        listener.receiveInvoiceResponse(message)

        verify(pendingInvoice, times(1)).unregister("corr-2", resp)
    }

    @Test
    fun `receiveInvoiceRequest invokes handlers for action`() {
        val converter: MessageConverter = mock()
        val invoiceHandler: InvoiceServiceHandler = mock()
        val invalidHandler: InvalidMessageActionHandler = mock()
        val pendingCustomer = spy(PendingCustomerMessages())
        val pendingInvoice = spy(PendingInvoiceMessages())

        val listener = MessageListener(converter, invoiceHandler, invalidHandler, pendingCustomer, pendingInvoice)

        val req = InvoiceRequest(requestId = "req1", invoiceId = 123L, action = "renderInvoice", payload = null)
        val props = MessageProperties()
        props.replyTo = "reply-qq"
        props.correlationId = "corr-req"
        val message = Message(ByteArray(0), props)

        whenever(converter.fromMessage(message)).thenReturn(req)

        listener.receiveInvoiceRequest(message)

        verify(invoiceHandler, times(1)).createXmlInvoice(req, "corr-req", "reply-qq")
        verify(invalidHandler, never()).handleInvalidMessageAction(any(), any(), any())
    }

    @Test
    fun `receiveInvoiceRequest invokes invalid handler for other action`() {
        val converter: MessageConverter = mock()
        val invoiceHandler: InvoiceServiceHandler = mock()
        val invalidHandler: InvalidMessageActionHandler = mock()
        val pendingCustomer = spy(PendingCustomerMessages())
        val pendingInvoice = spy(PendingInvoiceMessages())

        val listener = MessageListener(converter, invoiceHandler, invalidHandler, pendingCustomer, pendingInvoice)

        val req = InvoiceRequest(requestId = "req2", invoiceId = 5L, action = "doSomethingElse", payload = null)
        val props = MessageProperties()
        props.replyTo = "reply-qq"
        props.correlationId = "corr-req2"
        val message = Message(ByteArray(0), props)

        whenever(converter.fromMessage(message)).thenReturn(req)

        listener.receiveInvoiceRequest(message)

        verify(invalidHandler, times(1)).handleInvalidMessageAction(req, "corr-req2", "reply-qq")
        verify(invoiceHandler, never()).createXmlInvoice(any(), any(), any())
    }
}

