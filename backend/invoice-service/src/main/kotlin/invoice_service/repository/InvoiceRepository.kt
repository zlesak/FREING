package invoice_service.repository

import invoice_service.model.Invoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface InvoiceRepository : JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    fun existsByInvoiceNumber(invoiceNumber: String): Boolean
}
