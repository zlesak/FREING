package invoice_service.dtos.reports.requests

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "Filtry pro generování reportu faktur")
data class InvoiceReportRequest(
    @field:Schema(description = "Seznam ID faktur (pokud je zadán, použijí se pouze tyto)")
    val invoiceIds: List<Long>? = null,
    @field:Schema(description = "ID zákazníka")
    val customerId: Long,
    @field:Schema(description = "Datum vystavení od (včetně)")
    val issueDateFrom: LocalDate? = null,
    @field:Schema(description = "Datum vystavení do (včetně)")
    val issueDateTo: LocalDate? = null,
    @field:Schema(description = "Minimální částka")
    val minAmount: BigDecimal? = null,
    @field:Schema(description = "Maximální částka")
    val maxAmount: BigDecimal? = null
)