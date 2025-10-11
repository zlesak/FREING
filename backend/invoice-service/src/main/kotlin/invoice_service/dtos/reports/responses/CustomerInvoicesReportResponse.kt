package invoice_service.dtos.reports.responses

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Souhrn pro jednoho zákazníka")
data class CustomerInvoicesReportResponse(
    @field:Schema(description = "Název zákazníka")
    val customerName: String,
    @field:Schema(description = "Počet faktur zákazníka")
    val invoiceCount: Int,
    @field:Schema(description = "Celková částka faktur zákazníka")
    val totalAmount: BigDecimal
)
