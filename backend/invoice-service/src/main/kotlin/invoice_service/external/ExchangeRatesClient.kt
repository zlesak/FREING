package invoice_service.external

import java.math.BigDecimal

interface ExchangeRatesClient {
    fun getRate(from: String, to: String): BigDecimal
}

