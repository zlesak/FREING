package invoice_service.services

import com.uhk.fim.prototype.common.exceptions.OperationDeniedException
import com.uhk.fim.prototype.common.security.JwtUserPrincipal
import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.dtos.reports.responses.AggregatedReportResponse
import invoice_service.dtos.reports.responses.AllInvoicesReportResponse
import invoice_service.dtos.reports.responses.CustomerInvoicesReportResponse
import invoice_service.messaging.handlers.CustomerServiceHandler
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.repository.InvoiceRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ReportingSubService(
    private val repo: InvoiceRepository,
    private val customerServiceHandler: CustomerServiceHandler
) {

    private fun List<Invoice>.totalAmount(): BigDecimal =
        fold(BigDecimal.ZERO) { acc, inv -> acc + inv.amount }

    private fun buildSpecification(request: InvoiceReportRequest): Specification<Invoice> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (request.customerId > 0) {
                predicates += cb.equal(root.get<Long>("customerId"), request.customerId)
            }
            request.referenceNumber?.takeIf { it.isNotBlank() }?.let { ref ->
                predicates += cb.like(cb.lower(root.get("referenceNumber")), "%${ref.lowercase()}%")
            }
            request.issueDateFrom?.let { from ->
                predicates += cb.greaterThanOrEqualTo(root.get("issueDate"), from)
            }
            request.issueDateTo?.let { to ->
                predicates += cb.lessThanOrEqualTo(root.get("issueDate"), to)
            }
            request.dueDateFrom?.let { from ->
                predicates += cb.greaterThanOrEqualTo(root.get("dueDate"), from)
            }
            request.dueDateTo?.let { to ->
                predicates += cb.lessThanOrEqualTo(root.get("dueDate"), to)
            }
            request.minAmount?.let { min ->
                predicates += cb.greaterThanOrEqualTo(root.get("amount"), min)
            }
            request.maxAmount?.let { max ->
                predicates += cb.lessThanOrEqualTo(root.get("amount"), max)
            }
            request.currency?.takeIf { it.isNotBlank() }?.let { cur ->
                predicates += cb.equal(cb.lower(root.get("currency")), cur.lowercase())
            }
            request.status?.let { status ->
                predicates += cb.equal(root.get<InvoiceStatusEnum>("status"), status)
            }

            cb.and(*predicates.toTypedArray())
        }

    fun generateAggregatedReport(invoices: List<Invoice>): AggregatedReportResponse {
        val perCustomer = invoices
            .groupBy { it.customerId }
            .map { (id, list) ->
                CustomerInvoicesReportResponse(
                    customerName = customerServiceHandler.getCustomerNameById(id),
                    invoiceCount = list.size,
                    totalAmount = list.totalAmount()
                )
            }

        val invoiceSummaries = invoices.map { inv ->
            AllInvoicesReportResponse(
                id = inv.id!!,
                invoiceNumber = inv.invoiceNumber,
                referenceNumber = inv.referenceNumber,
                customerId = inv.customerId,
                issueDate = inv.issueDate,
                dueDate = inv.dueDate,
                amount = inv.amount,
                currency = inv.currency,
                status = inv.status
            )
        }

        return AggregatedReportResponse(
            generatedAt = LocalDateTime.now(),
            totalInvoices = invoices.size,
            totalAmount = invoices.totalAmount(),
            perCustomer = perCustomer,
            invoices = invoiceSummaries
        )
    }

    fun makeAggregatedReportByFilter(request: InvoiceReportRequest): AggregatedReportResponse =
        generateAggregatedReport(getFilteredInvoices(request))

    fun generateAggregatedReportCsv(request: InvoiceReportRequest): ByteArray {
        val report = makeAggregatedReportByFilter(request)

        fun csvEscape(value: String): String =
            if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' })
                '"' + value.replace("\"", "\"\"") + '"'
            else value

        val csv = buildString {
            append("generatedAt,").append(csvEscape(report.generatedAt.toString())).append('\n')
            append("totalInvoices,").append(report.totalInvoices).append('\n')
            append("totalAmount,").append(report.totalAmount.toPlainString()).append('\n')
            append('\n')

            append("customerName,invoiceCount,totalAmount\n")
            report.perCustomer.forEach { c ->
                append(csvEscape(c.customerName)).append(',')
                    .append(c.invoiceCount).append(',')
                    .append(c.totalAmount.toPlainString()).append('\n')
            }
            append('\n')
            append("id,invoiceNumber,referenceNumber,customerId,issueDate,dueDate,amount,currency,status\n")
            report.invoices.forEach { inv ->
                append(inv.id)
                    .append(',').append(csvEscape(inv.invoiceNumber))
                    .append(',').append(csvEscape(inv.referenceNumber ?: ""))
                    .append(',').append(inv.customerId)
                    .append(',').append(csvEscape(inv.issueDate.toString()))
                    .append(',').append(csvEscape(inv.dueDate.toString()))
                    .append(',').append(inv.amount.toPlainString())
                    .append(',').append(csvEscape(inv.currency))
                    .append(',').append(inv.status.name)
                    .append('\n')
            }
        }
        return csv.toByteArray(Charsets.UTF_8)
    }

    fun getFilteredInvoices(request: InvoiceReportRequest): List<Invoice> {
        val authentication = SecurityContextHolder.getContext().authentication
        val customerId = if (authentication.authorities.any { it.authority == "ROLE_CUSTOMER" }) {
            (authentication.principal as? JwtUserPrincipal)?.also { request.customerId = it.id } ?: throw OperationDeniedException(
                message = "Customer not found"
            )
        } else null

        val invoices = when {
            !request.invoiceIds.isNullOrEmpty() -> repo.findAllById(request.invoiceIds)
            else -> repo.findAll(buildSpecification(request))
        }

        return customerId?.let { id ->
            invoices.filter { it.customerId == id.id }
        } ?: invoices
    }
}