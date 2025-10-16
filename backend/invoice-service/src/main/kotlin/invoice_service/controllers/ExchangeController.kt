package invoice_service.controllers

import invoice_service.dtos.rates.responses.CurrencyConversionResponse
import invoice_service.services.ExchangeRateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode

@Tag(name = "Exchange", description = "Externí směnné kurzy - převod částek")
@RestController
@RequestMapping("/api/exchange")
class ExchangeController(private val exchangeRateService: ExchangeRateService) {

    @Operation(summary = "Převést částku", description = "Převede částku z jedné měny do druhé podle nejnovějšího kurzu.")
    @ApiResponse(responseCode = "200", description = "Úspěšná konverze", content = [Content(schema = Schema(implementation = CurrencyConversionResponse::class))])
    @GetMapping("/convert")
    fun convert(
        @Parameter(description = "Zdrojová měna", example = "EUR") @RequestParam from: String,
        @Parameter(description = "Cílová měna", example = "CZK") @RequestParam to: String,
        @Parameter(description = "Částka", example = "100.0") @RequestParam amount: BigDecimal
    ): CurrencyConversionResponse {
        val result = exchangeRateService.convert(from, to, amount)
        val rateOut = result.rate.setScale(8, RoundingMode.HALF_UP)
        return CurrencyConversionResponse(from.uppercase(), to.uppercase(), amount, rateOut, result.converted)
    }
}
