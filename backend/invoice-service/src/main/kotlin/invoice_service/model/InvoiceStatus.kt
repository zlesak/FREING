package invoice_service.model

enum class InvoiceStatus {
    DRAFT,
    PENDING,
    PAID,
    CANCELLED,
    OVERDUE
}