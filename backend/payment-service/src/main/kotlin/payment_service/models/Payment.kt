package payment_service.models

import jakarta.persistence.*
import java.time.LocalDateTime

enum class PaymentStatus {
    CREATED,
    PENDING,
    APPROVED,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val invoiceId: Long,

    @Column(nullable = false)
    val amount: Double,

    @Column(nullable = false, length = 3)
    val currency: String,

    @Column(length = 500)
    val description: String? = null,

    @Column(length = 500)
    val returnUrl: String? = null,

    @Column(length = 500)
    val cancelUrl: String? = null,

    @Column(unique = true)
    var orderId: String? = null,

    @Column
    var captureId: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.CREATED,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
