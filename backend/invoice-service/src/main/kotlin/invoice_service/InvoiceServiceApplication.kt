package invoice_service

import invoice_service.config.ExchangeRatesProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
	scanBasePackages = [
		"invoice_service",
		"com.uhk.fim.prototype.common"
	]
)
@EnableConfigurationProperties(ExchangeRatesProperties::class)
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
class InvoiceServiceApplication

fun main(args: Array<String>) {
	runApplication<InvoiceServiceApplication>(*args)
}
