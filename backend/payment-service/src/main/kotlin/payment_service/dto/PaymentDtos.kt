package payment_service.dto

data class CreatePaymentRequest(
    val invoiceId: Long,
    val invoiceNumber: String,
    val referenceNumber: String,
    val amount: Double,
    val currency: String,
    val description: String?,
    val returnUrl: String,
    val cancelUrl: String
)

data class CreatePaymentResponse(
    val paymentId: Long,
    val orderId: String,
    val approvalUrl: String?,
    val status: String
)

data class CapturePaymentRequest(
    val orderId: String
)

data class CapturePaymentResponse(
    val paymentId: Long,
    val orderId: String,
    val status: String,
    val captureId: String?
)

data class PaymentStatusResponse(
    val paymentId: Long,
    val invoiceId: Long,
    val amount: Double,
    val currency: String,
    val status: String,
    val orderId: String?,
    val createdAt: String
)

