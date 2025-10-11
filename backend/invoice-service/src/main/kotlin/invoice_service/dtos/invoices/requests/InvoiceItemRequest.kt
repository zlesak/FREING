package invoice_service.dtos.invoices.requests

import io.swagger.v3.oas.annotations.media.Schema
import invoice_service.models.invoices.InvoiceItem
import java.math.BigDecimal

@Schema(description = "Položka faktury")
data class InvoiceItemRequest(
    @field:Schema(description = "ID položky", example = "1")
    val id: Long? = null,
    @field:Schema(description = "Popis položky", example = "Konzultace")
    val description: String,
    @field:Schema(description = "Množství", example = "2")
    val quantity: Int,
    @field:Schema(description = "Jednotková cena", example = "7500.00")
    val unitPrice: BigDecimal,
    @field:Schema(description = "Celková cena", example = "15000.00")
    val totalPrice: BigDecimal
) {
    fun toInvoiceItem(): InvoiceItem {
        return InvoiceItem(
            description = this.description,
            quantity = this.quantity,
            unitPrice = this.unitPrice,
            totalPrice = this.totalPrice,
        )
    }
}
