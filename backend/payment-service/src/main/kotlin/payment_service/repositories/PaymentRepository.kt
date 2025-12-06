package payment_service.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import payment_service.models.Payment

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): Payment?
    fun findByInvoiceId(invoiceId: Long): List<Payment>
}

