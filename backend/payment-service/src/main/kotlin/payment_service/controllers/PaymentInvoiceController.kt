package payment_service.controllers

import com.uhk.fim.prototype.common.messaging.dto.RenderingResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import payment_service.messaging.MessageSender
import payment_service.messaging.MessageListener
import java.util.concurrent.TimeUnit
import org.springframework.http.ResponseEntity
import java.util.Base64

@RestController
class PaymentInvoiceController(
    private val messageSender: MessageSender,
    private val messageListener: MessageListener
) {
    @GetMapping("/invoice/{id}") //TODO: vyměnit za pořádný endpoint, tohle je jen testovací teď
    fun renderInvoice(@PathVariable id: Long): ResponseEntity<Any> {
        val future = messageListener.registerFuture()
        val correlationId = future.first

        messageSender.sendRenderInvoiceRequest(id, correlationId)
        val response = try {
            // Čekání, teď 10 vteřin, rádoby timeout
            future.second.get(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            messageListener.removeFuture(correlationId)
            return ResponseEntity.status(504).body(mapOf("error" to "Timeout waiting for rendering response"))
        }

        val payload = if (response is RenderingResponse) response.payload else null
        val pdfBase64 = (payload as? Map<*, *>)?.get("pdfBase64") as? String
        if (response is RenderingResponse && response.status == "ok" && pdfBase64 != null) {
            val pdfBytes = Base64.getDecoder().decode(pdfBase64)
            return ResponseEntity
                .ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=invoice.pdf")
                .body(pdfBytes)
        }

        return ResponseEntity.status(502).body(mapOf("error" to "Failed to render invoice"))
    }
}
