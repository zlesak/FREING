package invoice_service.external

import java.math.BigDecimal

interface IExchangeRatesClient {
    fun getRate(from: String, to: String): BigDecimal
}

