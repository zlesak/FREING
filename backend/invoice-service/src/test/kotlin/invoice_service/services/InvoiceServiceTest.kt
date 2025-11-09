package invoice_service.services

import com.uhk.fim.prototype.common.exceptions.WrongDataException
import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceItemRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceItem
import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.repository.InvoiceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.fail
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*


class InvoiceServiceTest {
    private val repo: InvoiceRepository = mock()
    private val service = InvoiceService(repo)

    @Test
    fun `Create Invoice throws when invoice number exists`() {
        val req = InvoiceCreateRequest(
            invoiceNumber = "INV-1",
            issueDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(14),
            amount = BigDecimal("100.00"),
            items = emptyList()
        )
        doReturn(true).`when`(repo).existsByInvoiceNumber("INV-1")

        try {
            service.createInvoice(req)
            fail("Expected WrongDataException")
        } catch (_: WrongDataException) {
            // ok
        }
    }

    @Test
    fun `GetAllInvoices paginates correctly`() {
        val inv = (1..5).map {
            val i = Invoice()
            i.id = it.toLong()
            i.invoiceNumber = "INV-$it"
            i.issueDate = LocalDate.now()
            i.dueDate = LocalDate.now()
            i.amount = BigDecimal.valueOf(it.toLong())
            i.currency = "CZK"
            i
        }
        doReturn(inv).`when`(repo).findAll()

        val page = service.getAllInvoices(PageRequest.of(1, 2))
        assertEquals(2, page.size)
        assertEquals(5, page.totalElements)
        assertEquals(3, page.totalPages)
    }

    @Test
    fun `UpdateInvoice updates items and fields`() {
        val existing = Invoice()
        existing.id = 1L
        existing.invoiceNumber = "OLD"
        existing.items = mutableListOf(InvoiceItem().apply { id = 1L; name = "Item1" })

        val itemReq = InvoiceItemRequest(
            id = 1L,
            description = "Popis položky",
            name = "Item1Updated",
            unit = "ks",
            quantity = BigDecimal.ONE,
            unitPrice = BigDecimal("10.0"),
            totalPrice = BigDecimal("10.0"),
            vat = BigDecimal.ZERO
        )
        val req = InvoiceUpdateRequest(
            invoiceNumber = "NEW",
            referenceNumber = "REF",
            customerId = 5L,
            issueDate = LocalDate.now(),
            dueDate = LocalDate.now(),
            amount = BigDecimal("10.0"),
            currency = "EUR",
            status = InvoiceStatusEnum.PAID,
            items = listOf(itemReq)
        )

        doReturn(Optional.of(existing)).`when`(repo).findById(1L)
        doReturn(existing).`when`(repo).save(any())

        val updated = service.updateInvoice(1L, req)
        assertNotNull(updated)
        assertEquals("NEW", updated.invoiceNumber)
        assertEquals(1, updated.items.size)
        assertEquals("Item1Updated", updated.items[0].name)
    }

    @Test
    fun `DeleteInvoice deletes when exists`() {
        doReturn(true).`when`(repo).existsById(1L)
        service.deleteInvoice(1L)
        verify(repo).deleteById(1L)
    }
}

