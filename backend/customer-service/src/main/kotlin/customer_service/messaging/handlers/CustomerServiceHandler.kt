package customer_service.messaging.handlers

import com.uhk.fim.prototype.common.exceptions.customer.CustomerNotFoundException
import com.uhk.fim.prototype.common.exceptions.customer.CustomerServiceException
import com.uhk.fim.prototype.common.exceptions.invoice.InvoiceServiceException
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import customer_service.messaging.MessageSender
import customer_service.service.CustomerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CustomerServiceHandler @Autowired constructor(
    private val messageSender: MessageSender,
    private val customerService: CustomerService,
) {
    fun getCustomer(
        request: CustomerRequest,
        correlationId: String,
        replyTo: String
    ) {
        var response: CustomerResponse? = null

        try {
            if (request.customerId == null) {
                throw CustomerServiceException("Customer ID is null in request")
            }

            val customer = customerService.getCustomerById(request.customerId ?: -1, true)
            val payload: Map<String, Any?> = customer.toMap()

            response = CustomerResponse(
                requestId = request.requestId,
                customerId = customer.id,
                status = "ok",
                payload = payload,
                error = null
            )

        } catch (ex: CustomerNotFoundException) {
            response = CustomerResponse(
                requestId = request.requestId,
                customerId = request.customerId,
                status = "not_found",
                payload = emptyMap(),
                error = "Failed to retrieve customer data: ${ex.message}"
            )
        } catch (ex: CustomerServiceException) {
            response = CustomerResponse(
                requestId = request.requestId,
                customerId = request.customerId,
                status = "error",
                payload = emptyMap(),
                error = "Customer ID is not set in request: ${ex.message}"
            )
        } catch (ex: Exception) {
            response = CustomerResponse(
                requestId = request.requestId,
                customerId = request.customerId,
                status = "error",
                payload = emptyMap(),
                error = "Unexpected exception in CustomerServiceHandler: ${ex.message}"
            )
        } finally {
            if (response != null) {
                messageSender.sendCustomerResponse(response, replyTo, correlationId)
            } else {
                throw InvoiceServiceException("ERROR: response is null in createXmlInvoice for requestId=${request.requestId}")
            }
        }

    }
}