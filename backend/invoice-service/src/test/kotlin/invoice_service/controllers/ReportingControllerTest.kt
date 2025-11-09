package invoice_service.controllers

import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.dtos.reports.responses.AggregatedReportResponse
import invoice_service.services.ReportingSubService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import java.math.BigDecimal
import java.time.LocalDateTime

class ReportingControllerTest {

    @Test
    fun `makeAggregatedReport returns aggregated response`() {
        val service: ReportingSubService = mock()
        val request = InvoiceReportRequest(customerId = 1L)
        val aggregated = AggregatedReportResponse(
            totalInvoices = 1, totalAmount = BigDecimal("100.00"),
            perCustomer = emptyList(),
            generatedAt = LocalDateTime.now(),
            invoices = emptyList()
        )
        whenever(service.makeAggregatedReportByFilter(request)).thenReturn(aggregated)

        val controller = ReportingController(service)
        val resp = controller.makeAggregatedReport(request)

        assertEquals(aggregated, resp.body)
        assertTrue(resp.statusCode.is2xxSuccessful)
    }

    @Test
    fun `exportAggregatedReportCsv sets csv headers and returns bytes`() {
        val service: ReportingSubService = mock()
        val request = InvoiceReportRequest(customerId = 1L)
        val csvBytes = "col1,col2\n1,2".toByteArray()
        whenever(service.generateAggregatedReportCsv(request)).thenReturn(csvBytes)

        val controller = ReportingController(service)
        val resp = controller.exportAggregatedReportCsv(request)

        assertTrue(resp.statusCode.is2xxSuccessful)
        val headers = resp.headers
        assertEquals("text/csv;charset=UTF-8", headers.contentType.toString())
        assertEquals("attachment; filename=invoice-report.csv", headers.getFirst(HttpHeaders.CONTENT_DISPOSITION))
        assertArrayEquals(csvBytes, resp.body)
    }
}

