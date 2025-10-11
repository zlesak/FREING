package invoice_service.external

import invoice_service.config.ExchangeRatesProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class ExchangeRatesClientImpl(
    private val props: ExchangeRatesProperties
) : ExchangeRatesClient {

    private val restTemplate = RestTemplate()
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
                ?: throw ExchangeRatesException("Empty response from exchange rates API", HttpStatus.BAD_GATEWAY)
            val baseNode = response[fromLower] as? Map<*, *> ?: throw ExchangeRatesException("Missing base currency node '$fromLower'", HttpStatus.BAD_GATEWAY)
            val raw = baseNode[to.lowercase()] ?: throw ExchangeRatesException("Rate ${from.uppercase()} -> ${to.uppercase()} not found", HttpStatus.BAD_REQUEST)
            val rate = when (raw) {
                is Number -> raw.toString().toBigDecimal()
                is String -> raw.toBigDecimalOrNull() ?: throw ExchangeRatesException("Invalid rate format", HttpStatus.BAD_GATEWAY)
                else -> throw ExchangeRatesException("Unsupported rate value type", HttpStatus.BAD_GATEWAY)
            }
            cache[key] = CacheEntry(rate, now.plusSeconds(props.cacheTtlSeconds))
            return rate
        } catch (e: ExchangeRatesException) {
            throw e
        } catch (e: RestClientException) {
            logger.error("Exchange rates API call failed", e)
            throw ExchangeRatesException("Failed to fetch exchange rate", HttpStatus.BAD_GATEWAY)
        }
    }
}



