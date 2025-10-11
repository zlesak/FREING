package invoice_service.dtos.reports.responses

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

@Schema(description = "Souhrnný report podle filtru")
data class AggregatedReportResponse(
    @field:Schema(description = "Datum a čas vygenerování reportu")
    val generatedAt: LocalDateTime,
    @field:Schema(description = "Celkový počet faktur")
    val totalInvoices: Int,
    @field:Schema(description = "Součtová částka všech faktur")
    val totalAmount: BigDecimal,
    @field:Schema(description = "Souhrn podle zákazníka")
    val perCustomer: List<CustomerInvoicesReportResponse>,
    @field:Schema(description = "Seznam základních informací o fakturách v reportu")
    val invoices: List<AllInvoicesReportResponse>
)
