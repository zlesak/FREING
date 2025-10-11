package invoice_service.external

import org.springframework.http.HttpStatus

class ExchangeRatesException(message: String, val status: HttpStatus): RuntimeException(message)