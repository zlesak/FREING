package invoice_service.dtos.reports.responses

import invoice_service.models.invoices.InvoiceStatusEnum
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "Základní informace o jedné faktuře v reportu")
data class AllInvoicesReportResponse(
    @field:Schema(description = "ID faktury")
    val id: Long,
    @field:Schema(description = "Číslo faktury")
    val invoiceNumber: String,
    @field:Schema(description = "Referenční číslo faktury")
    val referenceNumber: String?,
    @field:Schema(description = "ID zákazníka")
    val customerId: Long,
    @field:Schema(description = "Jméno zákazníka")
    val customerName: String?,
    @field:Schema(description = "ID dodavatele")
    val supplierId: Long?,
    @field:Schema(description = "Jméno dodavatele")
    val supplierName: String?,
    @field:Schema(description = "Datum vystavení faktury")
    val issueDate: LocalDate,
    @field:Schema(description = "Datum splatnosti faktury")
    val dueDate: LocalDate,
    @field:Schema(description = "Částka faktury")
    val amount: BigDecimal,
    @field:Schema(description = "Měna faktury")
    val currency: String,
    @field:Schema(description = "Stav faktury")
    val status: InvoiceStatusEnum
)
