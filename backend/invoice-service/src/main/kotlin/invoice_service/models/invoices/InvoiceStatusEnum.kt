package invoice_service.models.invoices

enum class InvoiceStatusEnum {
    DRAFT,
    PENDING,
    PAID,
    CANCELLED,
    OVERDUE
}