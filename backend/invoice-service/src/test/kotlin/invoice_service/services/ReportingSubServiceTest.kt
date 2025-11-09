package invoice_service.services

import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.messaging.servicesHandlers.CustomerServiceHandler
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceItem
import invoice_service.repository.InvoiceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class ReportingSubServiceTest {
    private val repo: InvoiceRepository = mock()
    private val customerHandler: CustomerServiceHandler = mock()
    private val service = ReportingSubService(repo, customerHandler)

    private fun invoice(
        id: Long,
        customerId: Long,
        invoiceNumber: String,
        amount: BigDecimal,
        issueDate: LocalDate
    ): Invoice {
        val i = Invoice()
        i.id = id
        i.customerId = customerId
        i.invoiceNumber = invoiceNumber
        i.amount = amount
        i.issueDate = issueDate
        i.currency = "CZK"
        i.items = listOf(InvoiceItem()) as MutableList<InvoiceItem>
        return i
    }

    @Test
    fun `GenerateAggregatedReport aggregates correctly`() {
        whenever(customerHandler.getCustomerNameById(1L)).thenReturn("Customer A")
        val invs = listOf(
            invoice(1L, 1L, "INV-1", BigDecimal("100.00"), LocalDate.now()),
            invoice(2L, 1L, "INV-2", BigDecimal("150.00"), LocalDate.now())
        )

        val report = service.generateAggregatedReport(invs)

        assertEquals(2, report.totalInvoices)
        assertEquals(BigDecimal("250.00"), report.totalAmount)
        assertEquals(1, report.perCustomer.size)
        assertEquals("Customer A", report.perCustomer.first().customerName)
    }

    @Test
    fun `GenerateAggregatedReportCsv returns non-empty CSV`() {
        whenever(repo.findAll()).thenReturn(
            listOf(
                invoice(1L, 1L, "INV-1", BigDecimal("100.00"), LocalDate.now())
            )
        )
        whenever(customerHandler.getCustomerNameById(1L)).thenReturn("Customer A")

        val request = InvoiceReportRequest(customerId = 1L)
        val csv = service.generateAggregatedReportCsv(request)
        val text = String(csv)
        assertTrue(text.contains("totalInvoices"))
        assertTrue(text.contains("Customer A"))
    }
}

