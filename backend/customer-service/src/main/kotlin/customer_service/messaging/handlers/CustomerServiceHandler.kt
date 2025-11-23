package customer_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import com.uhk.fim.prototype.common.exceptions.getErrorProps
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
            )

        } catch (ex: NotFoundException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.CUSTOMER,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.NOT_FOUND,
                payload = emptyMap(),
                error = ex.getErrorProps()
            )
        } catch (ex: WrongDataException) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.CUSTOMER,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                payload = emptyMap(),
                error = ex.getErrorProps()
            )
        } catch (ex: Exception) {
            response = MessageResponse(
                apiSourceService = request.apiSourceService,
                sourceService = SourceService.CUSTOMER,
                requestId = request.requestId,
                targetId = request.targetId,
                status = MessageStatus.ERROR,
                payload = emptyMap(),
                error = ex.getErrorProps()
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