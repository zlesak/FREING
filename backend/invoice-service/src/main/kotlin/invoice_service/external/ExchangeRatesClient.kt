package invoice_service.external

import com.uhk.fim.prototype.common.exceptions.BadGatewayException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import invoice_service.config.ExchangeRatesProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class ExchangeRatesClient(
    private val props: ExchangeRatesProperties,
    private val restTemplate: RestTemplate = RestTemplate()
) : IExchangeRatesClient {

    data class CacheEntry(val rate: BigDecimal, val expiresAt: Instant)

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override fun getRate(fromCode: String, toCode: String): BigDecimal {
        val key = "${fromCode.uppercase()}_${toCode.uppercase()}"
        val now = Instant.now()
        cache[key]?.let { if (it.expiresAt.isAfter(now)) return it.rate }

        val fromLower = fromCode.lowercase()
        val url = "${props.baseUrl}/${fromLower}.json"
        val response: Map<*, *> = restTemplate.getForObject(url, Map::class.java)
            ?: throw BadGatewayException("Empty response from exchange rates API")
        val baseNode = response[fromLower] as? Map<*, *>
            ?: throw WrongDataException("Missing base currency node '$fromLower'")
        val raw = baseNode[toCode.lowercase()]
            ?: throw WrongDataException("Rate ${fromCode.uppercase()} -> ${toCode.uppercase()} not found")
        val rate = when (raw) {
            is Number -> raw.toString().toBigDecimal()
            else -> throw WrongDataException("Unsupported rate value type")
        }
        cache[key] = CacheEntry(rate, now.plusSeconds(props.cacheTtlSeconds))
        return rate
    }
}
