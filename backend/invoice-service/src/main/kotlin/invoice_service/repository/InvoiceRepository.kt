package invoice_service.repository

import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceStatusEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface InvoiceRepository : JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    fun existsByInvoiceNumber(invoiceNumber: String): Boolean

    @Modifying
    @Transactional
    @Query("DELETE FROM Invoice i WHERE i.id = :id AND i.status = 'DRAFT'")
    fun deleteByIdIfDraft(id: Long): Int

    fun findByIdAndStatus(id: Long, status: InvoiceStatusEnum): Invoice?
}