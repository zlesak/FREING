package invoice_service.external

import com.uhk.fim.prototype.common.exceptions.BadGatewayException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import invoice_service.config.ExchangeRatesProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

class ExchangeRatesClientTest {

    private val props = ExchangeRatesProperties(
        baseUrl = "https://example.com/currencies",
        cacheTtlSeconds = 3600
    )

    private lateinit var restTemplate: RestTemplate
    private lateinit var client: ExchangeRatesClient

    @BeforeEach
    fun setup() {
        restTemplate = Mockito.mock(RestTemplate::class.java)
        client = ExchangeRatesClient(props, restTemplate)
    }

    @Test
    fun `getRate returns numeric rate from response`() {
        val response = mapOf(
            "eur" to mapOf("usd" to 1.23456)
        )
        whenever(restTemplate.getForObject("${props.baseUrl}/eur.json", Map::class.java)).thenReturn(response)

        val rate = client.getRate("EUR", "USD")
        assertEquals(BigDecimal("1.23456"), rate)
    }

    @Test
    fun `getRate caches value and returns cached on subsequent call`() {
        val response = mapOf(
            "eur" to mapOf("usd" to "1.50")
        )
        whenever(restTemplate.getForObject("${props.baseUrl}/eur.json", Map::class.java)).thenReturn(response)

        val rate1 = client.getRate("EUR", "USD")
        val rate2 = client.getRate("eur", "usd") // different case should use cache key

        assertEquals(BigDecimal("1.50"), rate1)
        assertEquals(rate1, rate2)

        Mockito.verify(restTemplate, Mockito.times(1)).getForObject("${props.baseUrl}/eur.json", Map::class.java)
    }

    @Test
    fun `getRate throws WrongDataException when target currency missing`() {
        val response = mapOf("eur" to mapOf("gbp" to 0.88))
        whenever(restTemplate.getForObject("${props.baseUrl}/eur.json", Map::class.java)).thenReturn(response)

        assertThrows(WrongDataException::class.java) {
            client.getRate("EUR", "USD")
        }
    }

    @Test
    fun `getRate throws BadGatewayException on invalid rate format`() {
        val response = mapOf("eur" to mapOf("usd" to "not-a-number"))
        whenever(restTemplate.getForObject("${props.baseUrl}/eur.json", Map::class.java)).thenReturn(response)

        assertThrows(BadGatewayException::class.java) {
            client.getRate("EUR", "USD")
        }
    }

    @Test
    fun `getRate wraps RestClientException into BadGatewayException`() {
        whenever(restTemplate.getForObject("${props.baseUrl}/eur.json", Map::class.java)).thenThrow(
            RestClientException(
                "timeout"
            )
        )

        assertThrows(BadGatewayException::class.java) {
            client.getRate("EUR", "USD")
        }
    }
}

