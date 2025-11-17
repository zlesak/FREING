package invoice_service.services

import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.dtos.reports.responses.AggregatedReportResponse
import invoice_service.dtos.reports.responses.AllInvoicesReportResponse
import invoice_service.dtos.reports.responses.CustomerInvoicesReportResponse
import invoice_service.messaging.handlers.CustomerServiceHandler
import invoice_service.models.invoices.Invoice
import invoice_service.repository.InvoiceRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ReportingSubService(
    private val repo: InvoiceRepository,
    private val customerServiceHandler: CustomerServiceHandler
) {

    private fun List<Invoice>.totalAmount(): BigDecimal =
        fold(BigDecimal.ZERO) { acc, inv -> acc + inv.amount }

    private fun buildSpecification(request: InvoiceReportRequest): Specification<Invoice> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (request.customerId > 0) {
                predicates += cb.equal(root.get<Long>("customerId"), request.customerId)
            }
            request.issueDateFrom?.let { from ->
                predicates += cb.greaterThanOrEqualTo(root.get("issueDate"), from)
            }
            request.issueDateTo?.let { to ->
                predicates += cb.lessThanOrEqualTo(root.get("issueDate"), to)
            }
            request.minAmount?.let { min ->
                predicates += cb.greaterThanOrEqualTo(root.get("amount"), min)
            }
            request.maxAmount?.let { max ->
                predicates += cb.lessThanOrEqualTo(root.get("amount"), max)
            }

            cb.and(*predicates.toTypedArray())
        }

    fun generateAggregatedReport(invoices: List<Invoice>): AggregatedReportResponse {
        val perCustomer = invoices
            .groupBy { it.customerId }
            .map { (id, list) ->
                CustomerInvoicesReportResponse(
                    invoiceCount = list.size,
                    totalAmount = list.totalAmount(),
                    customerName = customerServiceHandler.getCustomerNameById(id)
                )
            }

        val invoiceSummaries = invoices.map { inv ->
            AllInvoicesReportResponse(
                id = inv.id!!,
                invoiceNumber = inv.invoiceNumber,
                issueDate = inv.issueDate,
                amount = inv.amount,
                currency = inv.currency
            )
        }

        return AggregatedReportResponse(
            generatedAt = LocalDateTime.now(),
            totalInvoices = invoices.size,
            totalAmount = invoices.totalAmount(),
            perCustomer = perCustomer,
            invoices = invoiceSummaries
        )
    }

    fun makeAggregatedReportByFilter(request: InvoiceReportRequest): AggregatedReportResponse {
        val invoices = when {
            !request.invoiceIds.isNullOrEmpty() -> repo.findAllById(request.invoiceIds).toList()
            else -> repo.findAll(buildSpecification(request))
        }
        return generateAggregatedReport(invoices)
    }

    fun generateAggregatedReportCsv(request: InvoiceReportRequest): ByteArray {
        val report = makeAggregatedReportByFilter(request)

        fun csvEscape(value: String): String =
            if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' })
                '"' + value.replace("\"", "\"\"") + '"'
            else value

        val csv = buildString {
            append("generatedAt,").append(csvEscape(report.generatedAt.toString())).append('\n')
            append("totalInvoices,").append(report.totalInvoices).append('\n')
            append("totalAmount,").append(report.totalAmount.toPlainString()).append('\n')
            append('\n')

            append("customerName,invoiceCount,totalAmount\n")
            report.perCustomer.forEach { c ->
                append(csvEscape(c.customerName)).append(',')
                    .append(c.invoiceCount).append(',')
                    .append(c.totalAmount.toPlainString()).append('\n')
            }

            append('\n')

            append("id,invoiceNumber,issueDate,amount,currency\n")
            report.invoices.forEach { inv ->
                append(inv.id).append(',')
                    .append(csvEscape(inv.invoiceNumber)).append(',')
                    .append(csvEscape(inv.issueDate.toString())).append(',')
                    .append(inv.amount.toPlainString()).append(',')
                    .append(csvEscape(inv.currency)).append('\n')
            }
        }
        return csv.toByteArray(Charsets.UTF_8)
    }
}