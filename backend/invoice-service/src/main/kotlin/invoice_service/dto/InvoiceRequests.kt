package invoice_service.dto

import invoice_service.model.Invoice
import invoice_service.model.InvoiceItem
import invoice_service.model.InvoiceStatus
import java.math.BigDecimal
import java.time.LocalDate
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Požadavek na vytvoření faktury")
data class InvoiceCreateRequest(
    @Schema(description = "Číslo faktury", example = "20250001")
    val invoiceNumber: String,
    @Schema(description = "Jméno zákazníka", example = "Jan Novák")
    val customerName: String,
    @Schema(description = "Email zákazníka", example = "jan.novak@email.cz")
    val customerEmail: String,
    @Schema(description = "Datum vystavení faktury", example = "2025-10-05")
    val issueDate: LocalDate,
    @Schema(description = "Datum splatnosti faktury", example = "2025-11-05")
    val dueDate: LocalDate,
    @Schema(description = "Částka faktury", example = "15000.00")
    val amount: BigDecimal,
    @Schema(description = "Měna faktury", example = "CZK")
    val currency: String = "CZK",
    @Schema(description = "Stav faktury", example = "DRAFT")
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    @Schema(description = "Položky faktury")
    val items: List<InvoiceItemRequest>
)

@Schema(description = "Požadavek na aktualizaci faktury")
data class InvoiceUpdateRequest(
    @Schema(description = "Číslo faktury", example = "20250001")
    val invoiceNumber: String,
    @Schema(description = "Jméno zákazníka", example = "Jan Novák")
    val customerName: String,
    @Schema(description = "Email zákazníka", example = "jan.novak@email.cz")
    val customerEmail: String,
    @Schema(description = "Datum vystavení faktury", example = "2025-10-05")
    val issueDate: LocalDate,
    @Schema(description = "Datum splatnosti faktury", example = "2025-11-05")
    val dueDate: LocalDate,
    @Schema(description = "Částka faktury", example = "15000.00")
    val amount: BigDecimal,
    @Schema(description = "Měna faktury", example = "CZK")
    val currency: String = "CZK",
    @Schema(description = "Stav faktury", example = "DRAFT")
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    @Schema(description = "Položky faktury")
    val items: List<InvoiceItemRequest>
)

@Schema(description = "Položka faktury")
data class InvoiceItemRequest(
    @Schema(description = "ID položky", example = "1")
    val id: Long? = null,
    @Schema(description = "Popis položky", example = "Konzultace")
    val description: String,
    @Schema(description = "Množství", example = "2")
    val quantity: Int,
    @Schema(description = "Jednotková cena", example = "7500.00")
    val unitPrice: BigDecimal,
    @Schema(description = "Celková cena", example = "15000.00")
    val totalPrice: BigDecimal
)


fun InvoiceCreateRequest.toInvoice(): Invoice{
    return Invoice(
        invoiceNumber = this.invoiceNumber,
        customerName = this.customerName,
        customerEmail = this.customerEmail,
        issueDate = this.issueDate,
        dueDate = this.dueDate,
        amount = this.amount,
        currency = this.currency,
        status = this.status,
        items = this.items.map { it.toInvoiceItem() }.toMutableList()
        )
}


fun InvoiceItemRequest.toInvoiceItem(): InvoiceItem {
    return InvoiceItem(
        description = this.description,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice,
    )
}
