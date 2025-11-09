package invoice_service.services

import invoice_service.external.IExchangeRatesClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal


class ExchangeRateServiceTest {

    private val client: IExchangeRatesClient = mock()
    private val service = ExchangeRateService(client)

    @Test

    fun `Convert returns same amount and rate 1 when currencies equal ignoring case`() {
        val amount = BigDecimal("100.00")
        val result = service.convert("CZK", "czk", amount)
        assertEquals(amount, result.converted)
        assertEquals(BigDecimal.ONE, result.rate)
    }

    @Test
    fun `Convert uses client rate and scales result`() {
        val amount = BigDecimal("2.5")
        whenever(client.getRate("EUR", "USD")).thenReturn(BigDecimal("1.23456"))

        val result = service.convert("EUR", "USD", amount)

        assertEquals(BigDecimal("3.0864"), result.converted)
        assertEquals(BigDecimal("1.23456"), result.rate)
    }
}

