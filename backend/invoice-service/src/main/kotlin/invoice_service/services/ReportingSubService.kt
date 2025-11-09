package invoice_service.services

import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.dtos.reports.responses.AggregatedReportResponse
import invoice_service.dtos.reports.responses.AllInvoicesReportResponse
import invoice_service.dtos.reports.responses.CustomerInvoicesReportResponse
import invoice_service.messaging.handlers.CustomerServiceHandler
import invoice_service.models.invoices.Invoice
import invoice_service.repository.InvoiceRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ReportingSubService(
    private val repo: InvoiceRepository,
    private val customerServiceHandler: CustomerServiceHandler
) {

    fun generateAggregatedReport(invoices: List<Invoice>): AggregatedReportResponse {
        val totalInvoices = invoices.size
        val totalAmount = invoices.fold(BigDecimal.ZERO) { acc, inv -> acc + inv.amount }
        val perCustomer = invoices.groupBy { it.customerId }
            .map { (id, list) ->
                CustomerInvoicesReportResponse(
                    invoiceCount = list.size,
                    totalAmount = list.fold(BigDecimal.ZERO) { acc, inv -> acc + inv.amount },
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
            totalInvoices = totalInvoices,
            totalAmount = totalAmount,
            perCustomer = perCustomer,
            invoices = invoiceSummaries
        )
    }

    fun makeAggregatedReportByFilter(request: InvoiceReportRequest): AggregatedReportResponse {
        val invoices = if (!request.invoiceIds.isNullOrEmpty()) {
            repo.findAllById(request.invoiceIds).toList()
        } else {
            repo.findAll().filter { inv ->
                val byCustomer = if (request.customerId > 0) inv.customerId == request.customerId else true
                val byFrom = request.issueDateFrom?.let { !inv.issueDate.isBefore(it) } ?: true
                val byTo = request.issueDateTo?.let { !inv.issueDate.isAfter(it) } ?: true
                val byMin = request.minAmount?.let { inv.amount >= it } ?: true
                val byMax = request.maxAmount?.let { inv.amount <= it } ?: true
                byCustomer && byFrom && byTo && byMin && byMax
            }
        }
        return generateAggregatedReport(invoices)
    }

    fun generateAggregatedReportCsv(request: InvoiceReportRequest): ByteArray {
        val report = makeAggregatedReportByFilter(request)

        fun csvEscape(value: String): String {
            return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
                "\"${value.replace("\"", "\"\"")}\""
            } else value
        }

        val sb = StringBuilder()
        sb.append("generatedAt,").append(csvEscape(report.generatedAt.toString())).append("\n")
        sb.append("totalInvoices,").append(report.totalInvoices).append("\n")
        sb.append("totalAmount,").append(report.totalAmount.toPlainString()).append("\n")
        sb.append("\n")

        sb.append("customerName,invoiceCount,totalAmount\n")
        report.perCustomer.forEach { c ->
            sb.append(c.customerName).append(',')
                .append(c.invoiceCount).append(',')
                .append(c.totalAmount.toPlainString()).append('\n')
        }

        sb.append("\n")

        sb.append("id,invoiceNumber,issueDate,amount,currency\n")
        report.invoices.forEach { inv ->
            sb.append(inv.id).append(',')
                .append(csvEscape(inv.invoiceNumber)).append(',')
                .append(csvEscape(inv.issueDate.toString())).append(',')
                .append(inv.amount.toPlainString()).append(',')
                .append(csvEscape(inv.currency)).append('\n')
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}