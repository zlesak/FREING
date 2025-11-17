package invoice_service.dtos.reports.requests

import invoice_service.models.invoices.InvoiceStatusEnum
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "Filtry pro generování reportu faktur")
data class InvoiceReportRequest(
    @field:Schema(description = "Seznam ID faktur (pokud je zadán, použijí se pouze tyto)")
    val invoiceIds: List<Long>? = null,
    @field:Schema(description = "ID zákazníka", example = "42")
    val customerId: Long,
    @field:Schema(description = "Číslo faktury (částečná shoda)", example = "20250001")
    val invoiceNumber: String? = null,
    @field:Schema(description = "Referenční číslo faktury (částečná shoda)", example = "REF-2025-0001")
    val referenceNumber: String? = null,
    @field:Schema(description = "Datum vystavení od (včetně)", example = "2025-10-01")
    val issueDateFrom: LocalDate? = null,
    @field:Schema(description = "Datum vystavení do (včetně)", example = "2025-10-31")
    val issueDateTo: LocalDate? = null,
    @field:Schema(description = "Datum splatnosti od (včetně)", example = "2025-11-01")
    val dueDateFrom: LocalDate? = null,
    @field:Schema(description = "Datum splatnosti do (včetně)", example = "2025-11-30")
    val dueDateTo: LocalDate? = null,
    @field:Schema(description = "Minimální částka", example = "1000.00")
    val minAmount: BigDecimal? = null,
    @field:Schema(description = "Maximální částka", example = "50000.00")
    val maxAmount: BigDecimal? = null,
    @field:Schema(description = "Měna", example = "CZK")
    val currency: String? = null,
    @field:Schema(description = "Stav faktury", example = "DRAFT")
    val status: InvoiceStatusEnum? = null
)