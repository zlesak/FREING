package invoice_service.services

import invoice_service.models.invoices.InvoiceStatusEnum
import invoice_service.repository.InvoiceRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class InvoiceSchedulingService(private val invoiceRepository: InvoiceRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 1 * * ?") // Spustí se každý den v 1:00
    @Transactional
    fun updateOverdueInvoices() {
        logger.info("Running a task to update overdue invoices.")
        val today = LocalDate.now()
        val unpaidInvoices = invoiceRepository.findAllByStatusAndDueDateBefore(InvoiceStatusEnum.PENDING, today)

        if (unpaidInvoices.isEmpty()) {
            logger.info("No overdue invoices found to update.")
            return
        }

        unpaidInvoices.forEach { invoice ->
            invoice.status = InvoiceStatusEnum.OVERDUE
        }

        invoiceRepository.saveAll(unpaidInvoices)
        logger.info("Updated {} invoices to DUE status.", unpaidInvoices.size)
    }
}

