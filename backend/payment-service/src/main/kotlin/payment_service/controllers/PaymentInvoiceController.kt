package payment_service.controllers

import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.invoice.MessageInvoiceAction
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import payment_service.messaging.MessageListener
import payment_service.messaging.MessageSender
import java.util.*

@Tag(name = "Payments", description = "API pro správu platebních činností")
@RestController
@RequestMapping("/api/payments")
class PaymentInvoiceController(
    private val messageSender: MessageSender,
    private val messageListener: MessageListener,
) {
    @GetMapping("/invoice/{id}/render") //TODO: vyměnit za pořádný endpoint, tohle je jen testovací teď
    fun renderInvoice(@PathVariable id: Long): ResponseEntity<Any> {
        val response = messageSender.sendRenderInvoiceRequest(id, MessageInvoiceAction.RENDER, timeoutSeconds = 20)

        val pdfBase64 = (response.payload as? Map<*, *>)?.get("pdfBase64") as? String
        if (response.status == MessageStatus.OK && pdfBase64 != null) {
            val pdfBytes = Base64.getDecoder().decode(pdfBase64)
            return ResponseEntity
                .ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=invoice.pdf")
                .body(pdfBytes)
        }

        return ResponseEntity.status(502).body(mapOf("error" to "Failed to render invoice: ${response.error}"))
    }

    @PostMapping("/invoice/pay/{id}")
    fun pay(@PathVariable id: String): Boolean {
        return false
    }
}
