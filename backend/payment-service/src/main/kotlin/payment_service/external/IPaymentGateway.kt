package payment_service.external

import payment_service.dto.CreatePaymentRequest
import payment_service.dto.CreatePaymentResponse
import payment_service.dto.CapturePaymentRequest
import payment_service.dto.CapturePaymentResponse

interface IPaymentGateway {
    fun createOrder(request: CreatePaymentRequest): CreatePaymentResponse
    fun captureOrder(request: CapturePaymentRequest): CapturePaymentResponse
    fun getOrderStatus(orderId: String): String
    fun cancelOrder(orderId: String): Boolean
    fun getProviderName(): String
}

