package invoice_service.components.mappers

import invoice_service.dtos.reports.responses.AllInvoicesReportResponse
import invoice_service.dtos.reports.responses.CustomerInvoicesReportResponse
import invoice_service.extensions.totalAmount
import invoice_service.messaging.handlers.CustomerServiceHandler
import invoice_service.models.invoices.Invoice
import org.springframework.stereotype.Component


@Component
class InvoiceReportMapper(
    private val customerServiceHandler: CustomerServiceHandler
) {

    fun groupInvoicesByCustomer(invoices: List<Invoice>): List<CustomerInvoicesReportResponse> =
        invoices
            .groupBy { it.customerId }
            .map { (customerId, customerInvoices) ->
                CustomerInvoicesReportResponse(
                    customerName = customerServiceHandler.getCustomerNameById(customerId),
                    invoiceCount = customerInvoices.size,
                    totalAmount = customerInvoices.totalAmount()
                )
            }

    fun mapToInvoiceSummaries(invoices: List<Invoice>): List<AllInvoicesReportResponse> =
        invoices.map { invoice ->
            AllInvoicesReportResponse(
                id = invoice.id!!,
                invoiceNumber = invoice.invoiceNumber,
                referenceNumber = invoice.referenceNumber,
                customerId = invoice.customerId,
                issueDate = invoice.issueDate,
                dueDate = invoice.dueDate,
                amount = invoice.amount,
                currency = invoice.currency,
                status = invoice.status
            )
        }
}

