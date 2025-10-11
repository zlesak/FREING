package invoice_service.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "exchange.rates")
data class ExchangeRatesProperties(
    var baseUrl: String = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies",
    var cacheTtlSeconds: Long = 3600
)

