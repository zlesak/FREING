package invoice_service.messaging.handlers

import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.actions.CustomerMessageAction
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomerServiceRequestHandler(
    private val messageSender: MessageSender,
) {
    @Throws(NotFoundException::class)
    fun getCustomerNameById(
        customerId: Long
    ): String {
        val customerData = getCustomerById(customerId)
        val name = customerData["name"] as? String
        val surname = customerData["surname"] as? String
        return ("$name $surname")
    }

    @Throws(NotFoundException::class)
    fun getCustomerById(
        customerId: Long
    ): Map<String, Any> {
        val response = sendCustomerRequestAndReturnResponse(customerId)
        if (response.status == MessageStatus.OK) {
            @Suppress("UNCHECKED_CAST")
            val outerPayload = response.payload as Map<String, Any>

            @Suppress("UNCHECKED_CAST")
            val customerData = outerPayload["payload"] as? Map<String, Any>
                ?: throw NotFoundException("Customer data not found in response payload")
            return customerData
        } else {
            throw NotFoundException("Failed to get customer: ${response.error ?: "Unknown error"}")
        }
    }

    @Throws(NotFoundException::class)
    fun getSupplierById(
        supplierId: Long
    ): Map<String, Any> {
        val response = sendSupplierRequestAndReturnResponse(supplierId)
        if (response.status == MessageStatus.OK) {
            @Suppress("UNCHECKED_CAST")
            val outerPayload = response.payload as Map<String, Any>

            @Suppress("UNCHECKED_CAST")
            val supplierData = outerPayload["payload"] as? Map<String, Any>
                ?: throw NotFoundException("Supplier data not found in response payload")
            return supplierData
        } else {
            throw NotFoundException("Failed to get supplier: ${response.error ?: "Unknown error"}")
        }
    }

    fun sendCustomerRequestAndReturnResponse(
        customerId: Long
    ): MessageResponse {
        return messageSender.sendRequest(
            MessageRequest(
                route = RabbitConfig.CUSTOMER_REQUESTS,
                requestId = UUID.randomUUID().toString(),
                targetId = customerId,
                action = CustomerMessageAction.GET_CUSTOMER_BY_ID,
                payload = null
            )
        )
    }
    fun sendSupplierRequestAndReturnResponse(
        supplierId: Long
    ): MessageResponse {
        return messageSender.sendRequest(
            MessageRequest(
                route = RabbitConfig.CUSTOMER_REQUESTS,
                requestId = UUID.randomUUID().toString(),
                targetId = supplierId,
                action = CustomerMessageAction.GET_SUPPLIER_BY_ID,
                payload = null
            )
        )
    }
}