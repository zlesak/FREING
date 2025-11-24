package payment_service.controllers

import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transaction")
class TransactionController {

    @PostMapping("/create/{id}")
    fun create(@PathVariable("id") invoiceId: String): Boolean {
        return false
    }

    @PostMapping("/capture")
    fun capture(@Param("token") orderId: String): Boolean {
        return false
    }

    @PostMapping("/cancel}")
    fun cancel(@Param("token") orderId: String): Boolean {
        return false
    }
}