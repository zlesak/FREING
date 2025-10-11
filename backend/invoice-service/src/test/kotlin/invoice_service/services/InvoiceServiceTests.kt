package invoice_service.services

import invoice_service.dtos.invoices.requests.InvoiceCreateRequest
import invoice_service.dtos.invoices.requests.InvoiceItemRequest
import invoice_service.dtos.invoices.requests.InvoiceUpdateRequest
import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.repository.InvoiceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random
import org.junit.jupiter.api.assertThrows

@SpringBootTest
@Transactional
class InvoiceServiceTests(
    @Autowired private val service: InvoiceService,
    @Autowired private val repo: InvoiceRepository
) {
    private fun createSampleRequest(invoiceNumber: String = "INV-${Random.nextInt(100000)}"): InvoiceCreateRequest {
        return InvoiceCreateRequest(
            invoiceNumber = invoiceNumber,
            customerName = "Test Customer",
            customerEmail = "test@example.com",
            issueDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(14),
            amount = BigDecimal("123.45"),
            currency = "CZK",
            status = InvoiceStatusEnum.DRAFT,
            items = listOf(
                InvoiceItemRequest(description = "Item 1", quantity = 2, unitPrice = BigDecimal("10.00"), totalPrice = BigDecimal("20.00")),
                InvoiceItemRequest(description = "Item 2", quantity = 1, unitPrice = BigDecimal("50.00"), totalPrice = BigDecimal("50.00"))
            )
        )
    }

    @Test
    fun `createInvoice persists invoice and items`() {
        val request = createSampleRequest()
        val saved = service.createInvoice(request)
        assertThat(saved.id).isNotNull()
        assertThat(saved.items).hasSize(2)
        assertThat(repo.findById(saved.id!!)).isPresent
    }

    @Test
    fun `updateInvoice replaces items and fields`() {
        val original = service.createInvoice(createSampleRequest("INV-ORIG-${Random.nextInt(1000)}"))
        val updateRequest = InvoiceUpdateRequest(
            invoiceNumber = original.invoiceNumber + "-UPD",
            customerName = "Updated Name",
            customerEmail = "updated@example.com",
            issueDate = original.issueDate,
            dueDate = original.dueDate.plusDays(7),
            amount = BigDecimal("999.99"),
            currency = "EUR",
            status = InvoiceStatusEnum.PENDING,
            items = listOf(
                InvoiceItemRequest(
                    id = original.items.first().id,
                    description = "Item 1 Updated",
                    quantity = 5,
                    unitPrice = BigDecimal("9.00"),
                    totalPrice = BigDecimal("45.00")
                ),
                InvoiceItemRequest(
                    description = "New Item", quantity = 1, unitPrice = BigDecimal("100.00"), totalPrice = BigDecimal("100.00")
                )
            )
        )
        val updated = service.updateInvoice(original.id!!, updateRequest)
        assertThat(updated).isNotNull
        updated!!
        assertThat(updated.invoiceNumber).endsWith("-UPD")
        assertThat(updated.items).hasSize(2)
        assertThat(updated.items.any { it.description == "New Item" }).isTrue()
        assertThat(updated.items.any { it.description == "Item 1 Updated" }).isTrue()
    }

    @Test
    fun `createInvoice throws exception for duplicate invoice number`() {
        val request = createSampleRequest("DUPLICATE-123")
        service.createInvoice(request)
        val duplicateRequest = createSampleRequest("DUPLICATE-123")
        assertThrows<IllegalArgumentException> {
            service.createInvoice(duplicateRequest)
        }
    }

    @Test
    fun `deleteInvoice removes invoice`() {
        val request = createSampleRequest()
        val saved = service.createInvoice(request)
        service.deleteInvoice(saved.id!!)
        assertThat(repo.findById(saved.id!!)).isNotPresent
    }

    @Test
    fun `getInvoice returns invoice by id`() {
        val request = createSampleRequest()
        val saved = service.createInvoice(request)
        val found = service.getInvoice(saved.id!!)
        assertThat(found).isNotNull
        assertThat(found?.invoiceNumber).isEqualTo(saved.invoiceNumber)
    }

    @Test
    fun `getInvoice returns null for non-existing id`() {
        val found = service.getInvoice(-999L)
        assertThat(found).isNull()
    }

    @Test
    fun `getAllInvoices returns paged result`() {
        repeat(15) { service.createInvoice(createSampleRequest("PAGE-$it")) }
        val page = service.getAllInvoices(PageRequest.of(0, 10))
        assertThat(page.content).hasSize(10)
        assertThat(page.totalElements).isGreaterThanOrEqualTo(15)
        assertThat(page.totalPages).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun `createInvoice with no items works`() {
        val request = createSampleRequest().copy(items = emptyList())
        val saved = service.createInvoice(request)
        assertThat(saved.items).isEmpty()
    }

    @Test
    fun `updateInvoice can remove all items`() {
        val request = createSampleRequest()
        val saved = service.createInvoice(request)
        val updateRequest = InvoiceUpdateRequest(
            invoiceNumber = saved.invoiceNumber,
            customerName = saved.customerName,
            customerEmail = saved.customerEmail,
            issueDate = saved.issueDate,
            dueDate = saved.dueDate,
            amount = saved.amount,
            currency = saved.currency,
            status = saved.status,
            items = emptyList()
        )
        val updated = service.updateInvoice(saved.id!!, updateRequest)
        assertThat(updated!!.items).isEmpty()
    }

    @Test
    fun `deleteInvoice does nothing for non-existing id`() {
        service.deleteInvoice(-999L)
    }
}
