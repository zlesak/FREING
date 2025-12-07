package invoice_service.services

import com.uhk.fim.prototype.common.security.JwtUserPrincipal
import invoice_service.components.generators.CsvReportGenerator
import invoice_service.components.mappers.InvoiceReportMapper
import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.dtos.reports.responses.AggregatedReportResponse
import invoice_service.extensions.specifications
import invoice_service.extensions.totalAmount
import invoice_service.messaging.handlers.CustomerServiceRequestHandler
import invoice_service.messaging.handlers.RenderingServiceRequestHandler
import invoice_service.models.invoices.Invoice
import invoice_service.repository.InvoiceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReportingSubService(
    private val repo: InvoiceRepository,
    private val mapper: InvoiceReportMapper,
    private val csvGenerator: CsvReportGenerator,
    private val renderingServiceRequestHandler: RenderingServiceRequestHandler,
    private val customerServiceRequestHandler: CustomerServiceRequestHandler
) {

    companion object {
        private const val MAX_REPORT_SIZE = 10000
    }

    fun generateAggregatedReport(invoices: Page<Invoice>, limitReached: Boolean = false): AggregatedReportResponse {
        val invoiceList = invoices.content

        return AggregatedReportResponse(
            generatedAt = LocalDateTime.now(),
            totalInvoices = invoiceList.size,
            totalAmount = invoiceList.totalAmount(),
            perCustomer = mapper.groupInvoicesByCustomer(invoiceList),
            invoices = mapper.mapToInvoiceSummaries(invoiceList),
            limitReached = limitReached
        )
    }

    fun makeAggregatedReportByFilter(request: InvoiceReportRequest): AggregatedReportResponse {
        val invoices = getFilteredInvoicesPaged(request, PageRequest.of(0, MAX_REPORT_SIZE))
        val limitReached = invoices.totalElements >= MAX_REPORT_SIZE
        println(invoices.totalElements)
        println(MAX_REPORT_SIZE)
        println(invoices.totalElements >= MAX_REPORT_SIZE)
        return generateAggregatedReport(invoices, limitReached)
    }

    fun generateAggregatedReportCsv(request: InvoiceReportRequest): String {
        val report = makeAggregatedReportByFilter(request)
        return csvGenerator.generateCsv(report)
    }

    fun getFilteredInvoicesPaged(request: InvoiceReportRequest, pageable: Pageable): Page<Invoice> {
        applyCustomerFilterIfNeeded(request)

        return if (!request.invoiceIds.isNullOrEmpty()) {
            repo.findByIdInWithLimit(request.invoiceIds, pageable)
        } else {
            repo.findAll(request.specifications(), pageable)
        }
    }

    private fun applyCustomerFilterIfNeeded(request: InvoiceReportRequest) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication.authorities.any { it.authority == "ROLE_CUSTOMER" }) {
            (authentication.principal as? JwtUserPrincipal)?.let { request.customerId = it.id }
        }
    }

    fun renderAggregatedReportPdf(request: InvoiceReportRequest): ByteArray =
        renderingServiceRequestHandler.renderReportByInvoiceId(makeAggregatedReportByFilter(request), request)
}
