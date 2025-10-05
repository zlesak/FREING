package invoice_service.repository

import invoice_service.model.InvoiceItem
import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceItemRepository : JpaRepository<InvoiceItem, Long>
