package invoice_service.service

import invoice_service.dto.InvoiceCreateRequest
import invoice_service.dto.InvoiceUpdateRequest
import invoice_service.dto.PagedResponse
import invoice_service.model.Invoice
import invoice_service.model.InvoiceItem
import invoice_service.repository.InvoiceRepository
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Optional

@Service
class InvoiceService (
    private val repo: InvoiceRepository,
    private val modelMapper: ModelMapper
) {

    @Transactional
    fun createInvoice(request: InvoiceCreateRequest) : Invoice {
        if (repo.existsByInvoiceNumber(request.invoiceNumber)) {
            throw IllegalArgumentException("Faktura s tímto číslem již existuje.")
        }
        val invoice = modelMapper.map(request, Invoice::class.java)
        invoice.createdAt = LocalDateTime.now()
        invoice.updatedAt = LocalDateTime.now()
        invoice.items.forEach {
            it.id = null
        }
        return repo.save(invoice)
    }

    fun getInvoice(id: Long): Optional<Invoice> = repo.findById(id)

    fun getAllInvoices(pageable: Pageable) : PagedResponse<Invoice> {
        val allInvoices = repo.findAll()

        val startIndex = pageable.pageNumber * pageable.pageSize
        val endIndex = minOf(startIndex + pageable.pageSize, allInvoices.size)
        val pageContent = if (startIndex < allInvoices.size) {
            allInvoices.subList(startIndex, endIndex)
        } else {
            emptyList<Invoice>()
        }

        return PagedResponse(
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
            existingInvoice.updatedAt = LocalDateTime.now()

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
