package invoice_service.services

import invoice_service.external.IExchangeRatesClient
import invoice_service.models.rates.ConversionResult
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class ExchangeRateService(private val client: IExchangeRatesClient) {
    fun convert(from: String, to: String, amount: BigDecimal): ConversionResult {
        if (from.equals(to, ignoreCase = true)) {
            return ConversionResult(amount, BigDecimal.ONE)
        }
        val rate = client.getRate(from, to)
        val converted = amount.multiply(rate).setScale(4, RoundingMode.HALF_UP)
        return ConversionResult(converted, rate)
    }
}
