package invoice_service.repository

import invoice_service.models.invoices.InvoiceItem
import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceItemRepository : JpaRepository<InvoiceItem, Long>
