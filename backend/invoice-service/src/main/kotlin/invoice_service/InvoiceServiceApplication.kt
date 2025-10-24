package invoice_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import invoice_service.config.ExchangeRatesProperties

@SpringBootApplication(
	scanBasePackages = [
		"invoice_service",
		"com.uhk.fim.prototype.common"
	]
)
@EnableConfigurationProperties(ExchangeRatesProperties::class)
class InvoiceServiceApplication

fun main(args: Array<String>) {
	runApplication<InvoiceServiceApplication>(*args)
}
