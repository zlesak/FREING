package invoice_service.dtos.invoices.requests

import invoice_service.models.invoices.InvoiceStatusEnum
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "Požadavek na aktualizaci faktury")
data class InvoiceUpdateRequest(
    @field:Schema(description = "Číslo faktury", example = "20250001")
    val invoiceNumber: String,
    @field:Schema(description = "Referenční číslo faktury", example = "REF-2025-0001")
    var referenceNumber: String? = null,
    @field:Schema(description = "ID zákazníka", example = "42")
    var customerId: Long = 0,
    @field:Schema(description = "Datum vystavení faktury", example = "2025-10-05")
    val issueDate: LocalDate,
    @field:Schema(description = "Datum splatnosti faktury", example = "2025-11-05")
    val dueDate: LocalDate,
    @field:Schema(description = "Částka faktury", example = "15000.00")
    val amount: BigDecimal,
    @field:Schema(description = "Měna faktury", example = "CZK")
    val currency: String = "CZK",
    @field:Schema(description = "Stav faktury", example = "DRAFT")
    val status: InvoiceStatusEnum = InvoiceStatusEnum.DRAFT,
    @field:Schema(description = "Položky faktury")
    val items: List<InvoiceItemRequest>
)
