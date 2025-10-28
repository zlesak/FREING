package invoice_service.models.invoices

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Schema(description = "Faktura v systému")
@Entity
@Table(name = "invoices")
data class Invoice(
    @field:Schema(description = "ID faktury", example = "1")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:Schema(description = "Číslo faktury", example = "20250001")
    @Column(nullable = false, unique = true)
    var invoiceNumber: String = "",

    @field:Schema(description = "Jméno zákazníka", example = "Jan Novák")
    @Column(nullable = false)
    var customerName: String = "",

    @field:Schema(description = "Email zákazníka", example = "jan.novak@email.cz")
    @Column(nullable = false)
    var customerEmail: String = "",

    @field:Schema(description = "Datum vystavení faktury", example = "2025-10-05")
    @Column(nullable = false)
    var issueDate: LocalDate = LocalDate.now(),

    @field:Schema(description = "Datum splatnosti faktury", example = "2025-11-05")
    @Column(nullable = false)
    var dueDate: LocalDate = LocalDate.now().plusDays(30),

    @field:Schema(description = "Částka faktury", example = "15000.00")
    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @field:Schema(description = "Měna faktury", example = "CZK")
    @Column(nullable = false, length = 3)
    var currency: String = "CZK",

    @field:Schema(description = "Stav faktury", example = "DRAFT")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvoiceStatusEnum = InvoiceStatusEnum.DRAFT,

    @field:Schema(description = "Položky faktury")
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id")
    var items: MutableList<InvoiceItem> = mutableListOf(),

    @field:Schema(description = "Datum vytvoření záznamu", example = "2025-10-05T10:00:00")
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @field:Schema(description = "Datum poslední aktualizace", example = "2025-10-05T10:00:00")
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun addItem(item: InvoiceItem) {
        items.add(item)
    }

    fun removeItem(item: InvoiceItem) {
        items.remove(item)
    }
    fun toMap(objectMapper: ObjectMapper): Map<String, Any> = objectMapper.convertValue(this, object : TypeReference<Map<String, Any>>() {})
}
