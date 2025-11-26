package invoice_service.services

import invoice_service.external.IExchangeRatesClient
import invoice_service.models.rates.ConversionResult
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ExchangeRateService(private val client: IExchangeRatesClient) {
    fun convert(from: String, to: String, amount: BigDecimal): ConversionResult =
        ConversionResult(from, to, getRate(from, to), amount)

    fun getRate(from: String, to: String): BigDecimal =
        if (from.equals(to, ignoreCase = true)) BigDecimal.ONE else client.getRate(from, to)
}
