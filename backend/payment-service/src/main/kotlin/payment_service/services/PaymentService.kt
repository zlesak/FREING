package payment_service.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import payment_service.dto.CreatePaymentRequest
import payment_service.dto.CreatePaymentResponse
import payment_service.dto.CapturePaymentRequest
import payment_service.dto.CapturePaymentResponse
import payment_service.dto.PaymentStatusResponse
import payment_service.external.IPaymentGateway
import payment_service.messaging.handlers.InvoiceServiceRequestHandler
import payment_service.models.Payment
import payment_service.models.PaymentStatus
import payment_service.repositories.PaymentRepository
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val invoiceServiceRequestHandler: InvoiceServiceRequestHandler,
    private val paymentGateway: IPaymentGateway
) {
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    @Transactional
    fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {
        logger.info("Creating payment for invoice ${request.invoiceId}, amount: ${request.amount} ${request.currency}")

        val payment = Payment(
            invoiceId = request.invoiceId,
            amount = request.amount,
            currency = request.currency,
            description = request.description,
            returnUrl = request.returnUrl,
            cancelUrl = request.cancelUrl,
            status = PaymentStatus.CREATED
        )
        val savedPayment = paymentRepository.save(payment)

        try {
            val gatewayResponse = paymentGateway.createOrder(request)

            savedPayment.orderId = gatewayResponse.orderId
            savedPayment.status = PaymentStatus.PENDING
            savedPayment.updatedAt = LocalDateTime.now()
            paymentRepository.save(savedPayment)

            logger.info("Payment order created via ${paymentGateway.getProviderName()}: ${gatewayResponse.orderId}")

            return CreatePaymentResponse(
                paymentId = savedPayment.id,
                orderId = gatewayResponse.orderId,
                approvalUrl = gatewayResponse.approvalUrl,
                status = savedPayment.status.name
            )
        } catch (e: Exception) {
            logger.error("Failed to create payment order", e)
            savedPayment.status = PaymentStatus.FAILED
            savedPayment.updatedAt = LocalDateTime.now()
            paymentRepository.save(savedPayment)
            throw RuntimeException("Failed to create payment order: ${e.message}", e)
        }
    }

    @Transactional
    fun capturePayment(request: CapturePaymentRequest): CapturePaymentResponse {
        logger.info("Capturing payment for order: ${request.orderId}")

        val payment = paymentRepository.findByOrderId(request.orderId)
            ?: throw IllegalArgumentException("Payment not found for order: ${request.orderId}")

        if (payment.status == PaymentStatus.COMPLETED) {
            logger.warn("Payment ${payment.id} already completed")
            return CapturePaymentResponse(
                paymentId = payment.id,
                orderId = payment.orderId!!,
                status = payment.status.name,
                captureId = payment.captureId
            )
        }

        try {
            val gatewayResponse = paymentGateway.captureOrder(request)

            payment.status = PaymentStatus.COMPLETED
            payment.captureId = gatewayResponse.captureId
            payment.updatedAt = LocalDateTime.now()
            paymentRepository.save(payment)

            logger.info("Payment ${payment.id} captured successfully via ${paymentGateway.getProviderName()}")

            invoiceServiceRequestHandler.updateInvoicePaidStatus(payment.invoiceId)

            return CapturePaymentResponse(
                paymentId = payment.id,
                orderId = payment.orderId!!,
                status = payment.status.name,
                captureId = gatewayResponse.captureId
            )
        } catch (e: Exception) {
            logger.error("Failed to capture payment", e)
            payment.status = PaymentStatus.FAILED
            payment.updatedAt = LocalDateTime.now()
            paymentRepository.save(payment)
            throw RuntimeException("Failed to capture payment: ${e.message}", e)
        }
    }

    fun getPaymentStatus(paymentId: Long): PaymentStatusResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("Payment not found: $paymentId") }

        return PaymentStatusResponse(
            paymentId = payment.id,
            invoiceId = payment.invoiceId,
            amount = payment.amount,
            currency = payment.currency,
            status = payment.status.name,
            orderId = payment.orderId,
            createdAt = payment.createdAt.toString()
        )
    }

    @Transactional
    fun cancelPayment(paymentId: Long) {
        logger.info("Cancelling payment: $paymentId")

        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("Payment not found: $paymentId") }

        payment.status = PaymentStatus.CANCELLED
        payment.updatedAt = LocalDateTime.now()
        paymentRepository.save(payment)

        if (payment.orderId != null) {
            try {
                paymentGateway.cancelOrder(payment.orderId!!)
            } catch (e: Exception) {
                logger.warn("Failed to cancel order in gateway: ${e.message}")
            }
        }
    }
}