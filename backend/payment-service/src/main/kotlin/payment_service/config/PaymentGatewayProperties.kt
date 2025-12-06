package payment_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "payment.gateway")
data class PaymentGatewayProperties(
    var provider: PaymentProvider = PaymentProvider.MOCK,
    var mockDelayMs: Long = 1000,
    var mockSuccessRate: Double = 1.0,
    var paypalClientId: String = "",
    var paypalClientSecret: String = "",
    var paypalEnvironment: String = "SANDBOX"
)

enum class PaymentProvider {
    MOCK
}

