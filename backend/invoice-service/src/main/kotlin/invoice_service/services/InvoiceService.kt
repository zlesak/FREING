package invoice_service.services

import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.dtos.invoices.responses.InvoicesPagedResponse
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceItem
import invoice_service.repository.InvoiceRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class InvoiceService (
    private val repo: InvoiceRepository
) {

    @Transactional
    fun createInvoice(request: InvoiceCreateRequest) : Invoice {
        if (repo.existsByInvoiceNumber(request.invoiceNumber)) throw IllegalArgumentException("Faktura s tímto číslem již existuje.")

        val invoice = request.toInvoice()
        invoice.items.forEach {
            it.id = null
        }
        return repo.save(invoice)
    }

    fun getInvoice(id: Long): Invoice? = repo.findByIdOrNull(id)

    fun getAllInvoices(pageable: Pageable) : InvoicesPagedResponse<Invoice> {
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
        return repo.findById(id).map { existingInvoice ->
            existingInvoice.invoiceNumber = request.invoiceNumber
            existingInvoice.customerName = request.customerName
            existingInvoice.customerEmail = request.customerEmail
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
                if (itemRequest.id != null) {
                    val existingItem = existingInvoice.items.find { it.id == itemRequest.id }
                    if (existingItem != null) {
                        existingItem.description = itemRequest.description
                        existingItem.quantity = itemRequest.quantity
                        existingItem.unitPrice = itemRequest.unitPrice
                        existingItem.totalPrice = itemRequest.totalPrice
                    }
                } else {
                    val newItem = InvoiceItem()
                    newItem.description = itemRequest.description
                    newItem.quantity = itemRequest.quantity
                    newItem.unitPrice = itemRequest.unitPrice
                    newItem.totalPrice = itemRequest.totalPrice
                    existingInvoice.addItem(newItem)
                }
            }
            repo.save(existingInvoice)
        }.orElse(null)
    }

    @Transactional
    fun deleteInvoice(id: Long) {
        if (repo.existsById(id)) {
            repo.deleteById(id)
        }
    }
}
