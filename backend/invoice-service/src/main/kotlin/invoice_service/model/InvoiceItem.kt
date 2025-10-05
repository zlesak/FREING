package invoice_service.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "invoice_items")
class InvoiceItem(
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

    override fun toString(): String {
        return "InvoiceItem(id=$id, description='$description', quantity=$quantity, unitPrice=$unitPrice, totalPrice=$totalPrice)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvoiceItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
