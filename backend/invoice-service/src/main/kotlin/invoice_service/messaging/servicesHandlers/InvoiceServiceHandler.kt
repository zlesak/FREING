package invoice_service.messaging.servicesHandlers

import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.exceptions.invoice.InvoiceServiceException
import com.uhk.fim.prototype.common.exceptions.invoice.ZugfredInvoiceServiceException
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
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
        var response: InvoiceResponse? = null

        try {
            val invoice = invoiceService.getInvoice(request.invoiceId ?: -1)
            if (invoice == null) {
                throw InvoiceServiceException("Invoice with id ${request.invoiceId} not found")
            }

            val customer = customerServiceHandler.getCustomerById(invoice.customerId, 5)
            val xml = zugferdService.createInvoice(invoice, customer)

            response = InvoiceResponse(
                requestId = request.requestId,
                invoiceId = request.invoiceId,
                status = "ok",
                payload = mapOf("xml" to xml),
                error = null
            )
        } catch (ex: CustomerNotFoundException) {
            response = InvoiceResponse(
                requestId = request.requestId,
                invoiceId = request.invoiceId,
                status = "error",
                payload = null,
                error = "Failed to retrieve customer data: ${ex.message}"
            )
        } catch (ex: ZugfredInvoiceServiceException) {
            response = InvoiceResponse(
                requestId = request.requestId,
                invoiceId = request.invoiceId,
                status = "error",
                payload = null,
                error = "Failed to generate ZUGFeRD XML: ${ex.message}"
            )
        } catch (ex: InvoiceServiceException) {
            response = InvoiceResponse(
                requestId = request.requestId,
                invoiceId = request.invoiceId,
                status = "not_found",
                payload = null,
                error = "InvoiceServiceException: ${ex.message}"
            )
        } catch (ex: Exception) {
            response = InvoiceResponse(
                requestId = request.requestId,
                invoiceId = request.invoiceId,
                status = "error",
                payload = null,
                error = "Unexpected exception in invoiceServiceQueries: ${ex.message}"
            )
        } finally {
            if (response != null) {
                messageSender.sendInvoiceResponse(response, replyTo, correlationId)
            }
            else {
                throw InvoiceServiceException("ERROR: response is null in createXmlInvoice for requestId=${request.requestId}")
            }
        }
    }
}