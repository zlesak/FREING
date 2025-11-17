package invoice_service.models.rates

import io.swagger.v3.oas.annotations.media.Schema
import invoice_service.dtos.rates.responses.CurrencyConversionResponse
import invoice_service.extensions.roundRate
import java.math.BigDecimal

@Schema(description = "Výsledek konverze měny")
data class ConversionResult(
    @Schema(description = "Konvertovaná částka", example = "100")
    val converted: BigDecimal,
    @Schema(description = "Směnný kurz použitý pro konverzi", example = "23.4567")
    val rate: BigDecimal
) {
    fun toResponse(from: String, to: String, originalAmount: BigDecimal) =
        CurrencyConversionResponse(
            from = from.uppercase(),
            to = to.uppercase(),
            originalAmount = originalAmount,
            rate = rate.roundRate(),
            convertedAmount = converted
        )
}
