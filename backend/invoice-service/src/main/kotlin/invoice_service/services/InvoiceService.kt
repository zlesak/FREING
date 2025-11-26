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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class InvoiceService(
    private val repo: InvoiceRepository
) {

    @Transactional
    fun createInvoice(request: InvoiceCreateRequest): Invoice = request.toInvoice().also {
        if (repo.existsByInvoiceNumber(request.invoiceNumber)) {
            throw WrongDataException("Faktura s tímto číslem již existuje.")
        }
    }.let { repo.save(it) }

    fun getInvoice(id: Long, fromMessaging: Boolean = false): Invoice =
        repo.findByIdOrNull(id)?.takeUnless { fromMessaging && it.status == InvoiceStatusEnum.DRAFT }
            ?: throw NotFoundException("Faktura s ID $id nebyla nalezena")

    fun getAllInvoices(pageable: Pageable): Page<Invoice> = repo.findAll(pageable)

    fun getAllInvoicesForLoggedInCustomer(customerId: Long, pageable: Pageable): Page<Invoice> =
        repo.findAllByCustomerId(customerId, pageable)

    @Transactional
    fun updateInvoice(id: Long, request: InvoiceUpdateRequest): Invoice =
        repo.findByIdAndStatus(id, InvoiceStatusEnum.DRAFT)
            ?.apply {
                if (repo.existsByInvoiceNumberAndIdNot(request.invoiceNumber, id)) {
                    throw OperationDeniedException("Faktura s tímto číslem již existuje.")
                }
                updateFrom(request)
            }
            ?.let { repo.save(it) }
            ?: throw OperationDeniedException("Nelze upravit fakturu - buď neexistuje, nebo není ve stavu DRAFT")

    @Transactional
    fun deleteInvoice(id: Long) {
        val invoice = repo.findByIdAndStatus(id, InvoiceStatusEnum.DRAFT)
            ?: throw OperationDeniedException("Nelze smazat fakturu - buď neexistuje, nebo není ve stavu DRAFT")
        repo.delete(invoice)
    }
}
