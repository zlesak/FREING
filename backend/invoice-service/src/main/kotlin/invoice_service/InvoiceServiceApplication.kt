package invoice_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InvoiceServiceApplication

fun main(args: Array<String>) {
	runApplication<InvoiceServiceApplication>(*args)
}
