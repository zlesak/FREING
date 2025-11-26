package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.BadGatewayException
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.OperationDeniedException
import com.uhk.fim.prototype.common.exceptions.getErrorProps
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import com.uhk.fim.prototype.common.messaging.enums.TransactionPayloadType
import invoice_service.messaging.MessageSender
import invoice_service.models.invoices.InvoiceStatusEnum
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
        } catch (ex: NotFoundException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = ex.getErrorProps()
            )
        } catch (ex: BadGatewayException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = ex.getErrorProps()
            )
        } catch (ex: NotFoundException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.NOT_FOUND,
                error = ex.getErrorProps()
            )
        } catch (ex: Exception) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = ex.getErrorProps()
            )
        } finally {
            response?.let { messageSender.sendInvoiceResponse(it, replyTo, correlationId) }
                ?: throw NotFoundException("ERROR: response is null in createXmlInvoice for requestId=${request.requestId}")
        }
    }

    fun validateInvoiceTransaction(request: InvoiceRequest, correlationId: String, replyTo: String) {
        var response: MessageResponse? = null

        try {
            val invoice = invoiceService.getInvoice(request.targetId ?: -1, true)
            if (invoice.status != InvoiceStatusEnum.PENDING) throw OperationDeniedException("You are not allowed to pay this invoice, invoice is not in pending state!")

            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.OK,
                payload = mapOf(TransactionPayloadType.ALLOWED_TO_PAY.name to true, TransactionPayloadType.AMOUNT.name to invoice.amount)
            )
        } catch (ex: OperationDeniedException){
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                error = ex.getErrorProps()
            )
        } catch (ex: NotFoundException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.INVOICE,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.NOT_FOUND,
                error = ex.getErrorProps()
            )
        } finally {
            response?.let { messageSender.sendInvoiceResponse(it, replyTo, correlationId) }
                ?: throw NotFoundException("ERROR: response is null in validation invoice transaction for requestId=${request.requestId}")

        }
    }
}