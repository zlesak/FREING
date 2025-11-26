package payment_service.controllers

import com.uhk.fim.prototype.common.exceptions.OperationDeniedException
import com.uhk.fim.prototype.common.security.JwtUserPrincipal
import org.springframework.data.repository.query.Param
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import payment_service.services.TransactionService

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService,
) {

    @PostMapping("/create/{id}")
    fun create(@PathVariable("id") invoiceId: Long, @AuthenticationPrincipal principal: JwtUserPrincipal) {
        println("actual user ${principal.username} with id ${principal.id}")
        return transactionService.create(invoiceId, principal.id?: throw OperationDeniedException("Signed user ${principal.username} don't have id"))
    }

    @PostMapping("/capture")
    fun capture(@Param("token") orderId: String): Boolean {
        return false
    }

    @PostMapping("/cancel")
    fun cancel(@Param("token") orderId: String): Boolean {
        return false
    }
}