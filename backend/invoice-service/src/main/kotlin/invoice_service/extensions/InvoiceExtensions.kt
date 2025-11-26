package invoice_service.extensions

import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.models.invoices.Invoice
import invoice_service.models.invoices.InvoiceStatusEnum
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal

fun List<Invoice>.totalAmount(): BigDecimal =
    fold(BigDecimal.ZERO) { acc, invoice -> acc + invoice.amount }

fun InvoiceReportRequest.specifications() : Specification<Invoice> =
    Specification { root, _, cb ->
        val predicates = mutableListOf<Predicate>()

        if (customerId > 0) {
            predicates += cb.equal(root.get<Long>("customerId"), customerId)
        }

        referenceNumber?.takeIf { it.isNotBlank() }?.let { ref ->
            predicates += cb.like(cb.lower(root.get("referenceNumber")), "%${ref.lowercase()}%")
        }

        issueDateFrom?.let { from ->
            predicates += cb.greaterThanOrEqualTo(root.get("issueDate"), from)
        }

        issueDateTo?.let { to ->
            predicates += cb.lessThanOrEqualTo(root.get("issueDate"), to)
        }

        dueDateFrom?.let { from ->
            predicates += cb.greaterThanOrEqualTo(root.get("dueDate"), from)
        }

        dueDateTo?.let { to ->
            predicates += cb.lessThanOrEqualTo(root.get("dueDate"), to)
        }

        minAmount?.let { min ->
            predicates += cb.greaterThanOrEqualTo(root.get("amount"), min)
        }

        maxAmount?.let { max ->
            predicates += cb.lessThanOrEqualTo(root.get("amount"), max)
        }

        currency?.takeIf { it.isNotBlank() }?.let { cur ->
            predicates += cb.equal(cb.lower(root.get("currency")), cur.lowercase())
        }

        status?.let { status ->
            predicates += cb.equal(root.get<InvoiceStatusEnum>("status"), status)
        }

        cb.and(*predicates.toTypedArray())
    }