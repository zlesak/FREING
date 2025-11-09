package invoice_service.controllers

import invoice_service.dtos.rates.responses.CurrencyConversionResponse
import invoice_service.models.rates.ConversionResult
import invoice_service.services.ExchangeRateService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.math.RoundingMode

class ExchangeControllerTest {

    @Test
    fun `convert returns converted response with uppercased currencies and scaled rate`() {
        val service: ExchangeRateService = mock()
        val inputAmount = BigDecimal("100.0")
        val expectedRate = BigDecimal("1.23456789")
        val expectedConverted = BigDecimal("123.456789")
        whenever(service.convert("EUR", "CZK", inputAmount)).thenReturn(
            ConversionResult(expectedConverted, expectedRate)
        )

        val controller = ExchangeController(service)
        val resp: CurrencyConversionResponse = controller.convert("EUR", "CZK", inputAmount)

        assertEquals("EUR", resp.from)
        assertEquals("CZK", resp.to)
        assertEquals(inputAmount, resp.originalAmount)
        // rate in controller is scaled to 8 places
        assertEquals(expectedRate.setScale(8, RoundingMode.HALF_UP), resp.rate)
        assertEquals(expectedConverted, resp.convertedAmount)
    }
}

