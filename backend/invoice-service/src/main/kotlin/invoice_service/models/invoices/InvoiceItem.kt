package invoice_service.models.invoices

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "invoice_items")
data class InvoiceItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var description: String = "",

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 10, scale = 2)
    var totalPrice: BigDecimal = BigDecimal.ZERO
) {
    constructor() : this(null, "", 1, BigDecimal.ZERO, BigDecimal.ZERO)
}
