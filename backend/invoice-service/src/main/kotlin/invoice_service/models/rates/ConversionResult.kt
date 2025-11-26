package invoice_service.models.rates

import io.swagger.v3.oas.annotations.media.Schema
import invoice_service.dtos.rates.responses.CurrencyConversionResponse
import invoice_service.extensions.roundRate
import java.math.BigDecimal

@Schema(description = "Výsledek konverze měny")
data class ConversionResult(
    val from: String,
    val to: String,
    val rate: BigDecimal,
    val originalAmount: BigDecimal
) {
    fun toResponse() =
        CurrencyConversionResponse(
            from = from.uppercase(),
            to = to.uppercase(),
            originalAmount = originalAmount,
            rate = rate.roundRate(),
            convertedAmount = originalAmount * rate
        )
}
