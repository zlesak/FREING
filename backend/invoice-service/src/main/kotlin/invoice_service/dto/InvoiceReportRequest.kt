package invoice_service.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "Filtry pro generování reportu faktur")
data class InvoiceReportRequest(
    @Schema(description = "Seznam ID faktur (pokud je zadán, použijí se pouze tyto)")
    val invoiceIds: List<Long>? = null,
    @Schema(description = "Jméno zákazníka (částečné vyhledávání)")
    val customerName: String? = null,
    @Schema(description = "Datum vystavení od (včetně)")
    val issueDateFrom: LocalDate? = null,
    @Schema(description = "Datum vystavení do (včetně)")
    val issueDateTo: LocalDate? = null,
    @Schema(description = "Minimální částka")
    val minAmount: BigDecimal? = null,
    @Schema(description = "Maximální částka")
    val maxAmount: BigDecimal? = null
)

