package payment_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication(
    scanBasePackages = [
        "payment_service",
        "com.uhk.fim.prototype.common"
    ]
)
class PaymentServiceApplication

fun main(args: Array<String>) {
	runApplication<PaymentServiceApplication>(*args)
}
