package invoice_service.controllers

import invoice_service.dtos.rates.responses.CurrencyConversionResponse
import invoice_service.services.ExchangeRateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@Tag(name = "Exchange", description = "Externí směnné kurzy - převod částek")
@RestController
@PreAuthorize("hasAnyAuthority('SCOPE_service.call', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT')")
@RequestMapping("/exchange")
class ExchangeController(private val exchangeRateService: ExchangeRateService) {

    @Operation(
        summary = "Převést částku",
        description = "Převede částku z jedné měny do druhé podle nejnovějšího kurzu."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Úspěšná konverze",
        content = [Content(schema = Schema(implementation = CurrencyConversionResponse::class))]
    )
    @GetMapping("/convert")
    fun convert(
        @Parameter(description = "Zdrojová měna", example = "EUR")
        @RequestParam from: String,
        @Parameter(description = "Cílová měna", example = "CZK")
        @RequestParam to: String,
        @Parameter(description = "Částka k převedení", example = "100.0")
        @RequestParam amount: BigDecimal
    ): CurrencyConversionResponse =
        exchangeRateService.convert(from, to, amount).toResponse()

    @Operation(
        summary = "Získá kurz jednotlivých měn",
        description = "Získá kurz z jedné měny do druhé podle nejnovějšího kurzu."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Kurz",
        content = [Content(schema = Schema(implementation = CurrencyConversionResponse::class))]
    )
    @GetMapping("/getRate")
    fun getRate(
        @Parameter(description = "Zdrojová měna", example = "EUR")
        @RequestParam from: String,
        @Parameter(description = "Cílová měna", example = "CZK")
        @RequestParam to: String,
    ): CurrencyConversionResponse =
        exchangeRateService.convert(from, to, BigDecimal.ONE).toResponse()
}
