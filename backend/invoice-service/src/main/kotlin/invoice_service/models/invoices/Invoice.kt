package invoice_service.models.invoices

import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
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

    @field:Schema(description = "Referenční číslo faktury", example = "REF-2025-0001")
    @Column(nullable = true)
    var referenceNumber: String? = null,

    @field:Schema(description = "ID zákazníka", example = "42")
    @Column(nullable = false)
    var customerId: Long = 0,

    @field:Schema(description = "ID dodavatele", example = "42")
    @Column(nullable = false)
    var supplierId: Long = 0,

    @field:Schema(description = "Datum vystavení faktury", example = "2025-10-05")
    @Column(nullable = false)
    var issueDate: LocalDate = LocalDate.now(),

    @field:Schema(description = "Datum doručení faktury", example = "2025-10-05")
    @Column(nullable = true)
    var receiveDate: LocalDate? = null,

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

    fun updateFrom(request: InvoiceUpdateRequest) {
        this.invoiceNumber = request.invoiceNumber
        this.customerId = request.customerId
        this.supplierId = request.supplierId
        this.referenceNumber = request.referenceNumber
        this.issueDate = request.issueDate
        this.dueDate = request.dueDate
        this.amount = request.amount
        this.currency = request.currency
        this.status = request.status
        this.updatedAt = Instant.now()

        val requestItemsMap = request.items.associateBy { it.id }

        val itemsToRemove = this.items.filter { it.id !in requestItemsMap.keys }
        itemsToRemove.forEach { this.removeItem(it) }

        request.items.forEach { itemRequest ->
            val item = if (itemRequest.id != null) {
                this.items.find { it.id == itemRequest.id } ?: InvoiceItem()
            } else {
                InvoiceItem()
            }
            item.name = itemRequest.name
            item.description = itemRequest.description
            item.unit = itemRequest.unit
            item.quantity = itemRequest.quantity
            item.unitPrice = itemRequest.unitPrice
            item.totalPrice = itemRequest.totalPrice
            item.vatRate = itemRequest.vat
            if (item.id == null || !this.items.contains(item)) {
                this.addItem(item)
            }
        }
    }
}
