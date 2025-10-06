package invoice_service.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Souhrnný report podle filtru")
data class AggregatedReportResponse(
    @Schema(description = "Datum a čas vygenerování reportu")
    val generatedAt: LocalDateTime,
    @Schema(description = "Celkový počet faktur")
    val totalInvoices: Int,
    @Schema(description = "Součtová částka všech faktur")
    val totalAmount: BigDecimal,
    @Schema(description = "Souhrn podle zákazníka")
    val perCustomer: List<CustomerSummary>,
    @Schema(description = "Seznam základních informací o fakturách v reportu")
    val invoices: List<InvoiceSummary>
)

@Schema(description = "Souhrn pro jednoho zákazníka")
data class CustomerSummary(
    @Schema(description = "Název zákazníka")
    val customerName: String,
    @Schema(description = "Počet faktur zákazníka")
    val invoiceCount: Int,
    @Schema(description = "Celková částka faktur zákazníka")
    val totalAmount: BigDecimal
)

@Schema(description = "Základní informace o jedné faktuře v reportu")
data class InvoiceSummary(
    @Schema(description = "ID faktury")
    val id: Long,
    @Schema(description = "Číslo faktury")
    val invoiceNumber: String,
    @Schema(description = "Název zákazníka")
    val customerName: String,
    @Schema(description = "Datum vystavení faktury")
    val issueDate: LocalDate,
    @Schema(description = "Částka faktury")
    val amount: BigDecimal,
    @Schema(description = "Měna faktury")
    val currency: String
)

