package invoice_service.services

import com.uhk.fim.prototype.common.exceptions.WrongDataException
import com.uhk.fim.prototype.common.handlers.GlobalExceptionHandler
import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.dtos.invoices.responses.InvoicesPagedResponse
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceItem
import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.repository.InvoiceRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class InvoiceService(
    private val repo: InvoiceRepository
) {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @Transactional
    fun createInvoice(request: InvoiceCreateRequest): Invoice {
        if (repo.existsByInvoiceNumber(request.invoiceNumber)) {
            logger.warn("Faktura s tímto číslem již existuje.")
            throw WrongDataException("Faktura s tímto číslem již existuje.")
        }

        val invoice = request.toInvoice()
        invoice.items.forEach {
            it.id = null
        }
        return repo.save(invoice)
    }

fun getInvoice(id: Long, fromMessaging: Boolean = false): Invoice? {
        val invoice = repo.findByIdOrNull(id)
        return if (fromMessaging && invoice?.status == InvoiceStatusEnum.DRAFT) null else invoice
    }
    fun getAllInvoices(pageable: Pageable): InvoicesPagedResponse<Invoice> {
        val allInvoices = repo.findAll()

        val startIndex = pageable.pageNumber * pageable.pageSize
        val endIndex = minOf(startIndex + pageable.pageSize, allInvoices.size)
        val pageContent = if (startIndex < allInvoices.size) {
            allInvoices.subList(startIndex, endIndex)
        } else {
            emptyList<Invoice>()
        }

        return InvoicesPagedResponse(
            content = pageContent,
            totalElements = allInvoices.size.toLong(),
            totalPages = (allInvoices.size + pageable.pageSize - 1) / pageable.pageSize,
            page = pageable.pageNumber,
            size = pageable.pageSize
        )
    }

    @Transactional
    fun updateInvoice(id: Long, request: InvoiceUpdateRequest): Invoice? {
        return repo.findById(id).filter { it.status == InvoiceStatusEnum.DRAFT }.map { existingInvoice ->
            existingInvoice.invoiceNumber = request.invoiceNumber
            existingInvoice.customerId = request.customerId
            existingInvoice.referenceNumber = request.referenceNumber
            existingInvoice.issueDate = request.issueDate
            existingInvoice.dueDate = request.dueDate
            existingInvoice.amount = request.amount
            existingInvoice.currency = request.currency
            existingInvoice.status = request.status
            existingInvoice.updatedAt = Instant.now()

            val requestItemsMap = request.items.associateBy { it.id }
            val itemsToRemove = existingInvoice.items.filter { it.id !in requestItemsMap.keys }
            itemsToRemove.forEach { existingInvoice.removeItem(it) }

            request.items.forEach { itemRequest ->
                val item = if (itemRequest.id != null) {
                    existingInvoice.items.find { it.id == itemRequest.id } ?: InvoiceItem()
                } else {
                    InvoiceItem()
                }
                item.name = itemRequest.name
                item.description = itemRequest.description
                item.unit = itemRequest.unit
                item.quantity = itemRequest.quantity
                item.unitPrice = itemRequest.unitPrice
                item.totalPrice = itemRequest.totalPrice
                item.vatRate = itemRequest.vat
                if (item.id == null || !existingInvoice.items.contains(item)) {
                    existingInvoice.addItem(item)
                }
            }
            repo.save(existingInvoice)
        }.orElse(null)
    }

    @Transactional
    fun deleteInvoice(id: Long) : Boolean {
        val invoice = repo.findByIdOrNull(id)
        if (invoice != null && invoice.status == InvoiceStatusEnum.DRAFT) {
            repo.deleteById(id)
            return true
        }
        return false
    }
}
