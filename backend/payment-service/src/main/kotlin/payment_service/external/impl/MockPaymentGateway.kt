package payment_service.external.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import payment_service.config.PaymentGatewayProperties
import payment_service.dto.CreatePaymentRequest
import payment_service.dto.CreatePaymentResponse
import payment_service.dto.CapturePaymentRequest
import payment_service.dto.CapturePaymentResponse
import payment_service.external.IPaymentGateway
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Component
class MockPaymentGateway(
    private val properties: PaymentGatewayProperties
) : IPaymentGateway {

    private val logger = LoggerFactory.getLogger(MockPaymentGateway::class.java)

    private val orders = ConcurrentHashMap<String, MockOrder>()

    data class MockOrder(
        val orderId: String,
        var status: String,
        val amount: Double,
        val currency: String,
        val invoiceId: Long,
        val approvalUrl: String,
        var captureId: String? = null
    )

    override fun createOrder(request: CreatePaymentRequest): CreatePaymentResponse {
        logger.info("[MOCK] Creating order for invoice ${request.invoiceId}, amount: ${request.amount} ${request.currency}")

        simulateProcessing()

        val orderId = generateOrderId()
        val approvalUrl = generateApprovalUrl(orderId, request)

        val mockOrder = MockOrder(
            orderId = orderId,
            status = "CREATED",
            amount = request.amount,
            currency = request.currency,
            invoiceId = request.invoiceId,
            approvalUrl = approvalUrl
        )
        orders[orderId] = mockOrder

        logger.info("[MOCK] Order created: $orderId")

        return CreatePaymentResponse(
            paymentId = 0,
            orderId = orderId,
            approvalUrl = approvalUrl,
            status = "PENDING"
        )
    }

    override fun captureOrder(request: CapturePaymentRequest): CapturePaymentResponse {
        logger.info("[MOCK] Capturing order: ${request.orderId}")

        val order = orders[request.orderId]
            ?: throw IllegalArgumentException("Order not found: ${request.orderId}")

        simulateProcessing()

        val isSuccess = Random.nextDouble() < properties.mockSuccessRate

        if (!isSuccess) {
            logger.warn("[MOCK] Payment capture failed (simulated failure)")
            order.status = "FAILED"
            throw RuntimeException("Mock payment failed - simulated error for testing")
        }

        val captureId = generateCaptureId()
        order.status = "COMPLETED"
        order.captureId = captureId

        logger.info("[MOCK] Order captured successfully: ${request.orderId}, capture ID: $captureId")

        return CapturePaymentResponse(
            paymentId = 0,
            orderId = request.orderId,
            status = "COMPLETED",
            captureId = captureId
        )
    }

    override fun getOrderStatus(orderId: String): String {
        logger.info("[MOCK] Getting order status: $orderId")
        val order = orders[orderId] ?: return "NOT_FOUND"
        return order.status
    }

    override fun cancelOrder(orderId: String): Boolean {
        logger.info("[MOCK] Cancelling order: $orderId")
        val order = orders[orderId] ?: return false
        order.status = "CANCELLED"
        logger.info("[MOCK] Order cancelled: $orderId")
        return true
    }

    override fun getProviderName(): String = "MOCK"

    private fun simulateProcessing() {
        if (properties.mockDelayMs > 0) {
            Thread.sleep(properties.mockDelayMs)
        }
    }

    private fun generateOrderId(): String {
        return UUID.randomUUID().toString()
            .replace("-", "")
            .take(17)
            .uppercase()
    }

    private fun generateCaptureId(): String {
        return UUID.randomUUID().toString()
            .replace("-", "")
            .take(17)
            .uppercase()
    }

    private fun generateApprovalUrl(orderId: String, request: CreatePaymentRequest): String {
        val params = mutableListOf(
            "orderId=$orderId",
            "amount=${request.amount}",
            "currency=${request.currency}",
            "invoiceId=${request.invoiceId}",
            "returnUrl=${java.net.URLEncoder.encode(request.returnUrl, "UTF-8")}",
            "cancelUrl=${java.net.URLEncoder.encode(request.cancelUrl, "UTF-8")}"
        )

        if (request.description != null) {
            params.add("description=${java.net.URLEncoder.encode(request.description, "UTF-8")}")
        }

        return "/mock-approval?${params.joinToString("&")}"
    }
}

