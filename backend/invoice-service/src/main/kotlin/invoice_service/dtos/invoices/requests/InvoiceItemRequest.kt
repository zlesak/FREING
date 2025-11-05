package invoice_service.dtos.invoices.requests

import invoice_service.models.invoices.InvoiceItem
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Položka faktury")
data class InvoiceItemRequest(
    @field:Schema(description = "ID položky", example = "1")
    val id: Long? = null,
    @field:Schema(description = "Popis položky", example = "Konzultace programování")
    val description: String,
    @field:Schema(description = "Jméno položky", example = "Schůzka")
    val name: String,
    @field:Schema(description = "Jednotka", example = "hod")
    val unit: String,
    @field:Schema(description = "Množství", example = "2")
    val quantity: BigDecimal,
    @field:Schema(description = "Jednotková cena", example = "7500.00")
    val unitPrice: BigDecimal,
    @field:Schema(description = "Celková cena", example = "15000.00")
    val totalPrice: BigDecimal,
    @field:Schema(description = "DPH", example = "0.00")
    val vat: BigDecimal
) {
    fun toInvoiceItem(): InvoiceItem {
        return InvoiceItem(
            id = this.id,
            name = this.name,
            description = this.description,
            unit = this.unit,
            quantity = this.quantity,
            unitPrice = this.unitPrice,
            totalPrice = this.totalPrice,
            vatRate = this.vat
        )
    }
}
