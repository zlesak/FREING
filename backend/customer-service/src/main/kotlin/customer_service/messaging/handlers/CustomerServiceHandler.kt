package customer_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.WrongDataException
import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.SourceService
import customer_service.messaging.MessageSender
import customer_service.service.CustomerService
import org.springframework.stereotype.Component

@Component
class CustomerServiceHandler(
    private val messageSender: MessageSender,
    private val customerService: CustomerService,
) {
    fun getCustomer(
        request: CustomerRequest,
        correlationId: String,
        replyTo: String
    ) {
        var response: MessageResponse? = null

        try {
            if (request.targetId == null) {
                throw WrongDataException("Customer ID is null in request")
            }

            val customer = customerService.getCustomerById(request.targetId ?: -1, true)
            val payload: Map<String, Any?> = customer.toMap()

            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.CUSTOMER,
                requestId = request.requestId,
                targetId = customer.id,
                status = MessageStatus.OK,
                payload = payload,
                error = null
            )

        } catch (ex: CustomerNotFoundException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.CUSTOMER,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.NOT_FOUND,
                payload = emptyMap(),
                error = "Failed to retrieve customer data: ${ex.message}"
            )
        } catch (ex: WrongDataException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.CUSTOMER,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                payload = emptyMap(),
                error = "Customer ID is not set in request: ${ex.message}"
            )
        } catch (ex: Exception) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.CUSTOMER,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                payload = emptyMap(),
                error = "Unexpected exception in CustomerServiceHandler: ${ex.message}"
            )
        } finally {
            if (response != null) {
                messageSender.sendCustomerResponse(response, replyTo, correlationId)
            } else {
                throw WrongDataException("ERROR: response is null in createXmlInvoice for requestId=${request.requestId}")
            }
        }

    }
}