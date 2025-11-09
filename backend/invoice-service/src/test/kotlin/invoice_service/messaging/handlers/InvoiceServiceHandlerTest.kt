package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.InvoiceGenerator
import invoice_service.messaging.MessageSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class InvoiceServiceHandlerTest {

    @Test
    fun `createXmlInvoice happy path sends ok response`() {
        val invoiceService: invoice_service.services.InvoiceService = mock()
        val zugferdService: invoice_service.services.ZugferdService = mock()
        val customerHandler: CustomerServiceHandler = mock()
        val sender: MessageSender = mock()

        val handler = InvoiceServiceHandler(invoiceService, zugferdService, customerHandler, sender)

        val req = InvoiceRequest(requestId = "r1", invoiceId = 10L, action = "renderInvoice", payload = null)

        val invoice = InvoiceGenerator().invoice(10L, 5L)
        whenever(invoiceService.getInvoice(10L)).thenReturn(invoice)
        whenever(customerHandler.getCustomerById(5L, 5)).thenReturn(mapOf("name" to "A"))
        whenever(zugferdService.createInvoice(eq(invoice), any())).thenReturn("<xml/>")

        handler.createXmlInvoice(req, "corr-1", "reply-qq")

        val capt = argumentCaptor<InvoiceResponse>()
        verify(sender, times(1)).sendInvoiceResponse(capt.capture(), eq("reply-qq"), eq("corr-1"))
        val resp = capt.firstValue
        assertEquals("ok", resp.status)
        assertNotNull(resp.payload)
    }

    @Test
    fun `createXmlInvoice when invoice not found sends not_found`() {
        val invoiceService: invoice_service.services.InvoiceService = mock()
        val zugferdService: invoice_service.services.ZugferdService = mock()
        val customerHandler: CustomerServiceHandler = mock()
        val sender: MessageSender = mock()

        val handler = InvoiceServiceHandler(invoiceService, zugferdService, customerHandler, sender)

        val req = InvoiceRequest(requestId = "r2", invoiceId = 999L, action = "renderInvoice", payload = null)

        whenever(invoiceService.getInvoice(999L)).thenReturn(null)

        handler.createXmlInvoice(req, "corr-2", "reply-qq")

        val capt = argumentCaptor<InvoiceResponse>()
        verify(sender, times(1)).sendInvoiceResponse(capt.capture(), eq("reply-qq"), eq("corr-2"))
        val resp = capt.firstValue
        assertEquals("not_found", resp.status)
    }

    @Test
    fun `createXmlInvoice when customer not found sends error`() {
        val invoiceService: invoice_service.services.InvoiceService = mock()
        val zugferdService: invoice_service.services.ZugferdService = mock()
        val customerHandler: CustomerServiceHandler = mock()
        val sender: MessageSender = mock()

        val handler = InvoiceServiceHandler(invoiceService, zugferdService, customerHandler, sender)

        val req = InvoiceRequest(requestId = "r3", invoiceId = 20L, action = "renderInvoice", payload = null)

        val invoice = InvoiceGenerator().invoice(id = 20L, customerId = 77L)
        whenever(invoiceService.getInvoice(20L)).thenReturn(invoice)
        whenever(customerHandler.getCustomerById(77L, 5)).thenThrow(CustomerNotFoundException("no"))

        handler.createXmlInvoice(req, "corr-3", "reply-qq")

        val capt = argumentCaptor<InvoiceResponse>()
        verify(sender, times(1)).sendInvoiceResponse(capt.capture(), eq("reply-qq"), eq("corr-3"))
        val resp = capt.firstValue
        assertEquals("error", resp.status)
        assertTrue(resp.error!!.contains("Failed to retrieve customer data"))
    }
}

