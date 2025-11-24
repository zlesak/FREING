package payment_service.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "transaction")
data class Transaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, name = "invoice_id")
    val invoiceId: Long,

    @Column(nullable = false, unique = false, name = "amount")
    val amount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, unique = false, name = "status")
    val status: TransactionStatus = TransactionStatus.CREATED,

    @Column(name = "updated")
    val updated: Instant = Instant.now(),

    @Column(name = "created")
    val created: Instant = Instant.now(),
)

enum class TransactionStatus {
    CREATED, PENDING_APPROVED, APPROVED, CAPTURED, CANCELLED, FAILED, EXPIRED
}