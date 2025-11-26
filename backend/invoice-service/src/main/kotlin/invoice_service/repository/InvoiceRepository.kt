package invoice_service.repository

import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceStatusEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

interface InvoiceRepository : JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    fun existsByInvoiceNumber(invoiceNumber: String): Boolean

    @Modifying
    @Transactional
    @Query("DELETE FROM Invoice i WHERE i.id = :id AND i.status = 'DRAFT'")
    fun deleteByIdIfDraft(id: Long): Int

    fun findByIdAndStatus(id: Long, status: InvoiceStatusEnum): Invoice?

    fun findAllByCustomerId(customerId: Long, pageable: Pageable): Page<Invoice>

    fun findAllByStatusAndDueDateBefore(status: InvoiceStatusEnum, dueDate: LocalDate): List<Invoice>

    @Query("SELECT i FROM Invoice i WHERE i.id IN :ids")
    fun findByIdInWithLimit(ids: List<Long>, pageable: Pageable): Page<Invoice>
}