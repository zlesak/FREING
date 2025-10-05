package invoice_service.service

import invoice_service.model.Invoice
import invoice_service.dto.AggregatedReportResponse
import invoice_service.dto.CustomerSummary
import invoice_service.dto.InvoiceSummary
import invoice_service.repository.InvoiceRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.math.BigDecimal
import invoice_service.dto.InvoiceReportRequest

@Service
class ReportingSubService(private val repo: InvoiceRepository) {

    fun generateAggregatedReport(invoices: List<Invoice>): AggregatedReportResponse {
        val totalInvoices = invoices.size
        val totalAmount = invoices.fold(BigDecimal.ZERO) { acc, inv -> acc + inv.amount }
        val perCustomer = invoices.groupBy { it.customerName }
            .map { (name, list) ->
                CustomerSummary(
                    customerName = name,
                    invoiceCount = list.size,
                    totalAmount = list.fold(BigDecimal.ZERO) { acc, inv -> acc + inv.amount }
                )
            }
        val invoiceSummaries = invoices.map { inv ->
            InvoiceSummary(
                id = inv.id!!,
                invoiceNumber = inv.invoiceNumber,
                customerName = inv.customerName,
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
                val byCustomer = request.customerName?.let { inv.customerName.contains(it, ignoreCase = true) } ?: true
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
            sb.append(csvEscape(c.customerName)).append(',')
                .append(c.invoiceCount).append(',')
                .append(c.totalAmount.toPlainString()).append('\n')
        }

        sb.append("\n")

        sb.append("id,invoiceNumber,customerName,issueDate,amount,currency\n")
        report.invoices.forEach { inv ->
            sb.append(inv.id).append(',')
                .append(csvEscape(inv.invoiceNumber)).append(',')
                .append(csvEscape(inv.customerName)).append(',')
                .append(csvEscape(inv.issueDate.toString())).append(',')
                .append(inv.amount.toPlainString()).append(',')
                .append(csvEscape(inv.currency)).append('\n')
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}