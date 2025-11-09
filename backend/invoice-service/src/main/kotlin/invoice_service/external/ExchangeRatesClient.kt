package invoice_service.external

import com.uhk.fim.prototype.common.exceptions.AbstractResponseException
import com.uhk.fim.prototype.common.exceptions.BadGatewayException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import invoice_service.config.ExchangeRatesProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class ExchangeRatesClient(
    private val props: ExchangeRatesProperties,
    private val restTemplate: RestTemplate = RestTemplate()
) : IExchangeRatesClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    data class CacheEntry(val rate: BigDecimal, val expiresAt: Instant)
    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override fun getRate(from: String, to: String): BigDecimal {
        val key = "${from.uppercase()}_${to.uppercase()}"
        val now = Instant.now()
        cache[key]?.let { if (it.expiresAt.isAfter(now)) return it.rate }

        val fromLower = from.lowercase()
        val url = "${props.baseUrl}/${fromLower}.json"
        try {
            val response: Map<*, *> = restTemplate.getForObject(url, Map::class.java)
                ?: throw BadGatewayException("Empty response from exchange rates API")
            val baseNode = response[fromLower] as? Map<*, *> ?: throw BadGatewayException("Missing base currency node '$fromLower'")
            val raw = baseNode[to.lowercase()] ?: throw WrongDataException("Rate ${from.uppercase()} -> ${to.uppercase()} not found")
            val rate = when (raw) {
                is Number -> raw.toString().toBigDecimal()
                is String -> raw.toBigDecimalOrNull() ?: throw BadGatewayException("Invalid rate format")
                else -> throw BadGatewayException("Unsupported rate value type")
            }
            cache[key] = CacheEntry(rate, now.plusSeconds(props.cacheTtlSeconds))
            return rate
        } catch (e: AbstractResponseException) {
            throw e
        } catch (e: RestClientException) {
            logger.error("Exchange rates API call failed", e)
            throw BadGatewayException("Failed to fetch exchange rate")
        }
    }
}
