package invoice_service.external

import java.math.BigDecimal

interface IExchangeRatesClient {
    fun getRate(fromCode: String, toCode: String): BigDecimal
}

