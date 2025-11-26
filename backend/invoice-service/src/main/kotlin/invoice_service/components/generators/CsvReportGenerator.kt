package invoice_service.components.generators

import invoice_service.dtos.reports.responses.AggregatedReportResponse
import invoice_service.extensions.csvEscape
import org.springframework.stereotype.Component

@Component
class CsvReportGenerator {
    fun generateCsv(report: AggregatedReportResponse): String = buildString {
        appendReportHeader(report)
        appendCustomerSummary(report)
        appendInvoiceDetails(report)
    }

    private fun StringBuilder.appendReportHeader(report: AggregatedReportResponse) {
        append("generatedAt,").append(report.generatedAt.toString().csvEscape()).append('\n')
        append("totalInvoices,").append(report.totalInvoices).append('\n')
        append("totalAmount,").append(report.totalAmount.toPlainString()).append('\n')
        append('\n')
    }

    private fun StringBuilder.appendCustomerSummary(report: AggregatedReportResponse) {
        append("customerName,invoiceCount,totalAmount\n")
        report.perCustomer.forEach { customer ->
            append(customer.customerName.csvEscape())
                .append(',').append(customer.invoiceCount)
                .append(',').append(customer.totalAmount.toPlainString())
                .append('\n')
        }
        append('\n')
    }

    private fun StringBuilder.appendInvoiceDetails(report: AggregatedReportResponse) {
        append("id,invoiceNumber,referenceNumber,customerId,issueDate,dueDate,amount,currency,status\n")
        report.invoices.forEach { invoice ->
            append(invoice.id)
                .append(',').append(invoice.invoiceNumber.csvEscape())
                .append(',').append((invoice.referenceNumber ?: "").csvEscape())
                .append(',').append(invoice.customerId)
                .append(',').append(invoice.issueDate.toString().csvEscape())
                .append(',').append(invoice.dueDate.toString().csvEscape())
                .append(',').append(invoice.amount.toPlainString())
                .append(',').append(invoice.currency.csvEscape())
                .append(',').append(invoice.status.name)
                .append('\n')
        }
    }
}

