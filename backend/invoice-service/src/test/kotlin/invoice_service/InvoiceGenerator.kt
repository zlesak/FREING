package invoice_service

import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceItem
import java.math.BigDecimal
import java.time.LocalDate

class InvoiceGenerator {
    fun invoice(
        id: Long? = null,
        customerId: Long? = null,
        invoiceNumber: String? = null,
        amount: BigDecimal? = null,
        issueDate: LocalDate? = null
    ): Invoice {
        val i = Invoice()
        i.id = id ?: 1L
        i.customerId = customerId ?: 1
        i.invoiceNumber = invoiceNumber ?: "INV-2024-0001"
        i.amount = amount ?:  BigDecimal(1000)
        i.issueDate = issueDate ?: LocalDate.now()
        i.currency = "CZK"
        i.items = listOf(InvoiceItem()) as MutableList<InvoiceItem>
        return i
    }
}