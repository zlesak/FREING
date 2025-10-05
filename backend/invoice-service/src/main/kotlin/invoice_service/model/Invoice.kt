package invoice_service.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Faktura v systému")
@Entity
@Table(name = "invoices")
class Invoice(
    @Schema(description = "ID faktury", example = "1")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Schema(description = "Číslo faktury", example = "20250001")
    @Column(nullable = false, unique = true)
    var invoiceNumber: String = "",

    @Schema(description = "Jméno zákazníka", example = "Jan Novák")
    @Column(nullable = false)
    var customerName: String = "",

    @Schema(description = "Email zákazníka", example = "jan.novak@email.cz")
    @Column(nullable = false)
    var customerEmail: String = "",

    @Schema(description = "Datum vystavení faktury", example = "2025-10-05")
    @Column(nullable = false)
    var issueDate: LocalDate = LocalDate.now(),

    @Schema(description = "Datum splatnosti faktury", example = "2025-11-05")
    @Column(nullable = false)
    var dueDate: LocalDate = LocalDate.now().plusDays(30),

    @Schema(description = "Částka faktury", example = "15000.00")
    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Schema(description = "Měna faktury", example = "CZK")
    @Column(nullable = false, length = 3)
    var currency: String = "CZK",

    @Schema(description = "Stav faktury", example = "DRAFT")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvoiceStatus = InvoiceStatus.DRAFT,

    @Schema(description = "Položky faktury")
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id")
    var items: MutableList<InvoiceItem> = mutableListOf(),

    @Schema(description = "Datum vytvoření záznamu", example = "2025-10-05T10:00:00")
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Schema(description = "Datum poslední aktualizace", example = "2025-10-05T10:00:00")
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun addItem(item: InvoiceItem) {
        items.add(item)
    }

    fun removeItem(item: InvoiceItem) {
        items.remove(item)
    }

    override fun toString(): String {
        return "Invoice(id=$id, invoiceNumber='$invoiceNumber', customerName='$customerName', amount=$amount, status=$status)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Invoice) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
