package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.exceptions.invoice.ZugfredInvoiceServiceException
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import invoice_service.messaging.MessageSender
import org.springframework.stereotype.Component

@Component
class InvoiceServiceHandler(
    private val invoiceService: invoice_service.services.InvoiceService,
    private val zugferdService: invoice_service.services.ZugferdService,
    private val customerServiceHandler: CustomerServiceHandler,
    private val messageSender: MessageSender
) {

    fun createXmlInvoice(
        request: InvoiceRequest,
        correlationId: String,
        replyTo: String
    ) {
        var response: MessageResponse? = null

        try {
            val invoice = invoiceService.getInvoice(request.targetId ?: -1, true)

            val customer = customerServiceHandler.getCustomerById(invoice.customerId, request.apiSourceService)
            val xml = zugferdService.createInvoice(invoice, customer)

            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.OK,
                payload = mapOf("xml" to xml),
                error = null
            )
        } catch (ex: CustomerNotFoundException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = "Failed to retrieve customer data: ${ex.message}"
            )
        } catch (ex: ZugfredInvoiceServiceException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = "Failed to generate ZUGFeRD XML: ${ex.message}"
            )
        } catch (ex: NotFoundException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.NOT_FOUND,
                error = "InvoiceServiceException: ${ex.message}"
            )
        } catch (ex: Exception) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = "Unexpected exception in InvoiceServiceHandler: ${ex.message}"
            )
        } finally {
            response?.let { messageSender.sendInvoiceResponse(it, replyTo, correlationId) }
                ?: throw NotFoundException("ERROR: response is null in createXmlInvoice for requestId=${request.requestId}")
        }
    }
}