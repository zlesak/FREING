package invoice_service.services

import invoice_service.extensions.roundAmount
import invoice_service.external.IExchangeRatesClient
import invoice_service.models.rates.ConversionResult
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ExchangeRateService(private val client: IExchangeRatesClient) {
    fun convert(from: String, to: String, amount: BigDecimal): ConversionResult {
        val fromCode = from.uppercase()
        val toCode = to.uppercase()

        if (fromCode == toCode) {
            return ConversionResult(converted = amount, rate = BigDecimal.ONE)
        }

        val rate = client.getRate(fromCode, toCode)

        return ConversionResult(converted = (amount * rate).roundAmount(), rate = rate)
    }
}
