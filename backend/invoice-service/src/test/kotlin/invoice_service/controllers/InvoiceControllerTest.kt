package invoice_service.controllers

import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.InvoiceGenerator
import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.dtos.invoices.responses.InvoicesPagedResponse
import invoice_service.messaging.MessageSender
import invoice_service.messaging.pendingMessages.PendingInvoiceMessages
import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.services.InvoiceService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

class InvoiceControllerTest {

    @Test
    fun `getAllInvoices delegates to service and returns paged response`() {
        val service: InvoiceService = Mockito.mock()
        val pending: PendingInvoiceMessages = Mockito.mock()
        val sender: MessageSender = Mockito.mock()

        val invoices = listOf(InvoiceGenerator().invoice(1L))
        val paged = InvoicesPagedResponse(content = invoices, totalElements = 1, totalPages = 1, page = 0, size = 10)
        whenever(service.getAllInvoices(any())).thenReturn(paged)

        val controller = InvoiceController(service, pending, sender)
        val resp = controller.getAllInvoices(0, 10)

        assertEquals(paged, resp)
    }

    @Test
    fun `getInvoice returns ok when found and notFound when missing`() {
        val service: InvoiceService = Mockito.mock()
        val pending: PendingInvoiceMessages = Mockito.mock()
        val sender: MessageSender = Mockito.mock()

        val invoice = InvoiceGenerator().invoice(5L)
        whenever(service.getInvoice(5L)).thenReturn(invoice)
        whenever(service.getInvoice(6L)).thenReturn(null)

        val controller = InvoiceController(service, pending, sender)

        val ok = controller.getInvoice(5L)
        assertTrue(ok.statusCode.is2xxSuccessful)
        assertEquals(invoice, ok.body)

        val notFound = controller.getInvoice(6L)
        assertTrue(notFound.statusCode.is4xxClientError)
    }

    @Test
    fun `createInvoice delegates to service`() {
        val service: InvoiceService = Mockito.mock()
        val pending: PendingInvoiceMessages = Mockito.mock()
        val sender: MessageSender = Mockito.mock()

        val req = InvoiceCreateRequest(
            invoiceNumber = "INV-1",
            issueDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(14),
            amount = BigDecimal("100.00"),
            items = emptyList()
        )
        val created = InvoiceGenerator().invoice(10L)
        whenever(service.createInvoice(req)).thenReturn(created)

        val controller = InvoiceController(service, pending, sender)
        val resp = controller.createInvoice(req)
        assertEquals(created, resp)
    }

    @Test
    fun `updateInvoice returns ok when service updates and notFound otherwise`() {
        val service: InvoiceService = Mockito.mock()
        val pending: PendingInvoiceMessages = Mockito.mock()
        val sender: MessageSender = Mockito.mock()

        val updateReq = InvoiceUpdateRequest(
            invoiceNumber = "NEW",
            referenceNumber = "REF",
            customerId = 2L,
            issueDate = LocalDate.now(),
            dueDate = LocalDate.now(),
            amount = BigDecimal("10.0"),
            currency = "CZK",
            status = InvoiceStatusEnum.DRAFT,
            items = emptyList()
        )

        val updated = InvoiceGenerator().invoice(7L)
        whenever(service.updateInvoice(7L, updateReq)).thenReturn(updated)
        whenever(service.updateInvoice(8L, updateReq)).thenReturn(null)

        val controller = InvoiceController(service, pending, sender)

        val ok = controller.updateInvoice(7L, updateReq)
        assertTrue(ok.statusCode.is2xxSuccessful)
        assertEquals(updated, ok.body)

        val nf = controller.updateInvoice(8L, updateReq)
        assertTrue(nf.statusCode.is4xxClientError)
    }

    @Test
    fun `deleteInvoice returns noContent and calls service delete`() {
        val service: InvoiceService = Mockito.mock()
        val pending: PendingInvoiceMessages = Mockito.mock()
        val sender: MessageSender = Mockito.mock()

        val controller = InvoiceController(service, pending, sender)
        val resp = controller.deleteInvoice(11L)
        assertTrue(resp.statusCode.is2xxSuccessful || resp.statusCode.value() == 204)
        Mockito.verify(service).deleteInvoice(11L)
    }

    @Test
    fun `getInvoiceXml registers future and returns xml when messageSender completes it`() {
        val service: InvoiceService = Mockito.mock()
        val pending: PendingInvoiceMessages = Mockito.mock()
        val sender: MessageSender = Mockito.mock()

        val futureRef = AtomicReference<CompletableFuture<InvoiceResponse>>()
        val idRef = AtomicReference<String>()

        doAnswer { inv ->
            idRef.set(inv.getArgument(0))
            futureRef.set(inv.getArgument(1))
            null
        }.whenever(pending).registerInvoiceResponseFuture(any(), any())

        doAnswer { _ ->
            // when message is sent, complete the previously registered future
            val f = futureRef.get()
            val resp = InvoiceResponse(
                requestId = idRef.get(),
                invoiceId = 3L,
                status = "ok",
                payload = mapOf("xml" to "<xml/>"),
                error = null
            )
            f.complete(resp)
            null
        }.whenever(sender).sendInvoiceRequest(any(), any())

        val controller = InvoiceController(service, pending, sender)
        val resp = controller.getInvoiceXml(3L)
        assertTrue(resp.statusCode.is2xxSuccessful)
        assertEquals("<xml/>", resp.body)
    }
}

