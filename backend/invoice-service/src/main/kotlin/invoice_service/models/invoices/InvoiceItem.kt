package invoice_service.models.invoices

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "invoice_items")
data class InvoiceItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var description: String = "",

    @Column(nullable = false)
    var unit: String = "",

    @Column(nullable = false)
    var quantity: BigDecimal = BigDecimal.ONE,

    @Column(nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 10, scale = 2)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var vatRate: BigDecimal = BigDecimal.ZERO,
) {
    constructor() : this(null, "", "", "", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
}