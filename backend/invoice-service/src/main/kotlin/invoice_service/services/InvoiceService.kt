package invoice_service.services

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.OperationDeniedException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.repository.InvoiceRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class InvoiceService(
    private val repo: InvoiceRepository
) {

    fun getFilteredInvoices(
        pageable: Pageable,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        customerId: Long?,
        status: InvoiceStatusEnum?,
        amountFrom: Double?,
        amountTo: Double?,
        currency: String?,
        isCustomer: Boolean = false
    ): Page<Invoice> {
        val specs = buildList {
            dateFrom?.let { add(Specification<Invoice> { root, _, cb -> cb.greaterThanOrEqualTo(root.get("issueDate"), it) }) }
            dateTo?.let { add(Specification<Invoice> { root, _, cb -> cb.lessThanOrEqualTo(root.get("issueDate"), it) }) }
            amountFrom?.let { add(Specification<Invoice> { root, _, cb -> cb.greaterThanOrEqualTo(root.get("totalAmount"), it) }) }
            amountTo?.let { add(Specification<Invoice> { root, _, cb -> cb.lessThanOrEqualTo(root.get("totalAmount"), it) }) }
            currency?.let { add(Specification<Invoice> { root, _, cb -> cb.equal(root.get<String>("currency"), it) }) }
            if (isCustomer && customerId != null) {
                add(Specification<Invoice> { root, _, cb -> cb.equal(root.get<Long>("customerId"), customerId) })
                add(Specification<Invoice> { root, _, cb -> cb.notEqual(root.get<InvoiceStatusEnum>("status"), InvoiceStatusEnum.DRAFT) })
            } else {
                customerId?.let { add(Specification<Invoice> { root, _, cb -> cb.equal(root.get<Long>("customerId"), it) }) }
                status?.let { add(Specification<Invoice> { root, _, cb -> cb.equal(root.get<InvoiceStatusEnum>("status"), it) }) }
            }
        }
        val spec = specs.reduceOrNull { acc, s -> acc.and(s) }
        return if (spec != null) repo.findAll(spec, pageable) else repo.findAll(pageable)
    }

    @Transactional
    fun createInvoice(request: InvoiceCreateRequest): Invoice = request.toInvoice().also {
        if (repo.existsByInvoiceNumber(request.invoiceNumber)) {
            throw WrongDataException("Invoice with this number already exists.")
        }
    }.let { repo.save(it) }

    fun getInvoice(id: Long, fromMessaging: Boolean = false): Invoice =
        repo.findByIdOrNull(id)?.takeUnless { fromMessaging && it.status == InvoiceStatusEnum.DRAFT }
            ?: throw NotFoundException("Invoice with ID $id was not found.")

    @Transactional
    fun updateInvoice(id: Long, request: InvoiceUpdateRequest): Invoice =
        repo.findByIdAndStatus(id, InvoiceStatusEnum.DRAFT)
            ?.apply {
                if (repo.existsByInvoiceNumberAndIdNot(request.invoiceNumber, id)) {
                    throw OperationDeniedException("Invoice with this number already exists.")
                }
                updateFrom(request)
            }
            ?.let { repo.save(it) }
            ?: throw OperationDeniedException("Cannot update invoice - either it does not exist or is not in DRAFT state.")

    @Transactional
    fun deleteInvoice(id: Long) {
        val invoice = repo.findByIdAndStatus(id, InvoiceStatusEnum.DRAFT)
            ?: throw OperationDeniedException("Cannot delete invoice - either it does not exist or is not in DRAFT state.")
        repo.delete(invoice)
    }
    @Transactional
    fun markInvoiceAsPayed(id: Long): Int = repo.markInvoiceAsPaid(id, LocalDate.now())
    @Transactional
    fun markInvoiceAsRead(id: Long): Int = repo.markInvoiceAsRead(id, LocalDate.now())
}
