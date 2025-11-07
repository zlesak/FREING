package invoice_service.models.invoices

enum class InvoiceStatusEnum {
    DRAFT,
    SENT,
    PENDING,
    PAID,
    CANCELLED,
    OVERDUE,
    PAID_OVERDUE
}