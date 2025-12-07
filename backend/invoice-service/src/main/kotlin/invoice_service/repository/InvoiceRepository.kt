package invoice_service.repository

import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceStatusEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface InvoiceRepository : JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    fun existsByInvoiceNumber(invoiceNumber: String): Boolean

    fun existsByInvoiceNumberAndIdNot(invoiceNumber: String, id: Long): Boolean

    fun findByIdAndStatus(id: Long, status: InvoiceStatusEnum): Invoice?

    fun findAllByCustomerIdAndStatusNot(customerId: Long, status: InvoiceStatusEnum, pageable: Pageable): Page<Invoice>

    fun findAllByStatusAndDueDateBefore(status: InvoiceStatusEnum, dueDate: LocalDate): List<Invoice>

    @Query("SELECT i FROM Invoice i WHERE i.id IN :ids")
    fun findByIdInWithLimit(ids: List<Long>, pageable: Pageable): Page<Invoice>

    @Modifying
    @Query("""
        UPDATE Invoice i 
        SET i.status = CASE 
            WHEN i.dueDate >= :currentDate THEN 'PAID' 
            ELSE 'PAID_OVERDUE' 
        END 
        WHERE i.id = :id
    """)
    fun markInvoiceAsPaid(id: Long, currentDate: LocalDate): Int

    @Modifying
    @Query("""
        UPDATE Invoice i 
        SET i.status = CASE 
                WHEN i.dueDate >= :currentDate THEN 'PENDING'
                ELSE 'OVERDUE'
            END,
            i.receiveDate = :currentDate
        WHERE i.id = :id AND i.status = 'SENT'
    """)
    fun markInvoiceAsRead(id: Long, currentDate: LocalDate): Int
}