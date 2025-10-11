package invoice_service.dtos.invoices.responses

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "InvoicesPagedResponse", description = "Stránkovaná odpověď s daty")
data class InvoicesPagedResponse<T>(
    @field:Schema(description = "Seznam položek na stránce")
    val content: List<T>,
    @field:Schema(description = "Celkový počet položek", example = "1")
    val totalElements: Long,
    @field:Schema(description = "Celkový počet stránek", example = "1")
    val totalPages: Int,
    @field:Schema(description = "Aktuální stránka", example = "0")
    val page: Int,
    @field:Schema(description = "Velikost stránky", example = "10")
    val size: Int
)