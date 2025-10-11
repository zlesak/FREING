package invoice_service.dtos.rates.responses

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "CurrencyConversionResponse", description = "Převod měny a související informace")

data class CurrencyConversionResponse(
    @field:Schema(description = "Zdrojová měna") val from: String,
    @field:Schema(description = "Cílová měna") val to: String,
    @field:Schema(description = "Původní částka") val originalAmount: BigDecimal,
    @field:Schema(description = "Konverzní kurz použitý pro výpočet") val rate: BigDecimal,
    @field:Schema(description = "Přepočtená částka") val convertedAmount: BigDecimal
)
