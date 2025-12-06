package payment_service.controllers

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.actions.RenderMessageAction
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import payment_service.dto.*
import payment_service.services.PaymentService
import java.util.*

@Tag(name = "Payments", description = "API pro správu platebních činností")
@RestController
@RequestMapping("/api/payments")
class PaymentInvoiceController(
    private val messageSender: MessageSender,
    private val paymentService: PaymentService
) {
    private val logger = LoggerFactory.getLogger(PaymentInvoiceController::class.java)

    @Operation(summary = "Vytvoření nové platby pro fakturu")
    @PostMapping("/create")
    fun createPayment(@RequestBody request: CreatePaymentRequest): CreatePaymentResponse =
        paymentService.createPayment(request)

    @Operation(summary = "Dokončení platby po schválení")
    @PostMapping("/capture")
    fun capturePayment(@RequestBody request: CapturePaymentRequest): CapturePaymentResponse =
        paymentService.capturePayment(request)

    @Operation(summary = "Získání stavu platby")
    @GetMapping("/{paymentId}/status")
    fun getPaymentStatus(@PathVariable paymentId: Long): PaymentStatusResponse =
        paymentService.getPaymentStatus(paymentId)

    @Operation(summary = "Zrušení platby")
    @PostMapping("/{paymentId}/cancel")
    fun cancelPayment(@PathVariable paymentId: Long): ResponseEntity<Map<String, Any>> {
        return try {
            paymentService.cancelPayment(paymentId)
            ResponseEntity.ok(mapOf("success" to true, "message" to "Payment cancelled"))
        } catch (e: Exception) {
            logger.error("Failed to cancel payment", e)
            ResponseEntity.status(500).body(mapOf("success" to false, "error" to (e.message ?: "Unknown error")))
        }
    }

    @GetMapping("/invoice/{id}/render")
    fun renderInvoice(@PathVariable id: Long): ResponseEntity<Any> {
        val response = messageSender.sendRequest(
            MessageRequest(
                route = RabbitConfig.RENDERING_REQUESTS,
                requestId = UUID.randomUUID().toString(),
                targetId = id,
                action = RenderMessageAction.RENDER,
                payload = null
            )
        )

        val pdfBase64 = (response.payload as? Map<*, *>)?.get("payload") as? String
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
}
