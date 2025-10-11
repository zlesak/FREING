package invoice_service.dtos.reports.responses

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "Základní informace o jedné faktuře v reportu")
data class AllInvoicesReportResponse(
    @field:Schema(description = "ID faktury")
    val id: Long,
    @field:Schema(description = "Číslo faktury")
    val invoiceNumber: String,
    @field:Schema(description = "Název zákazníka")
    val customerName: String,
    @field:Schema(description = "Datum vystavení faktury")
    val issueDate: LocalDate,
    @field:Schema(description = "Částka faktury")
    val amount: BigDecimal,
    @field:Schema(description = "Měna faktury")
    val currency: String
)
