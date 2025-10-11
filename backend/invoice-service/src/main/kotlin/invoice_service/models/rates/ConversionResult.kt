package invoice_service.models.rates

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Výsledek konverze měny")
data class ConversionResult(
    @Schema(description = "Konvertovaná částka", example = "100")
    val converted: BigDecimal,
    @Schema(description = "Směnný kurz použitý pro konverzi", example = "23.4567")
    val rate: BigDecimal
)
