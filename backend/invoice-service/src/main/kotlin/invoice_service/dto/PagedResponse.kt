package invoice_service.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Stránkovaná odpověď s daty")
data class PagedResponse<T>(
    @Schema(description = "Seznam položek na stránce")
    val content: List<T>,
    @Schema(description = "Celkový počet položek", example = "1")
    val totalElements: Long,
    @Schema(description = "Celkový počet stránek", example = "1")
    val totalPages: Int,
    @Schema(description = "Aktuální stránka", example = "0")
    val page: Int,
    @Schema(description = "Velikost stránky", example = "10")
    val size: Int
)
