package customer_service

import customer_service.config.AresProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication(
	scanBasePackages = [
		"customer_service",
		"com.uhk.fim.prototype.common"
	]
)
@EnableConfigurationProperties(AresProperties::class)
class CustomerServiceApplication

fun main(args: Array<String>) {
	runApplication<CustomerServiceApplication>(*args)
}
