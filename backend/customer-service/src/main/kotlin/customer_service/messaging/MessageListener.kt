package customer_service.messaging

import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import customer_service.models.CustomerEntity
import customer_service.service.CustomerService
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class MessageListener @Autowired constructor(
    private val messageSender: MessageSender,
    private val messageConverter: MessageConverter,
    private val customerService: CustomerService
) {

    @RabbitListener(queues = [RabbitConfig.CUSTOMER_REQUESTS])
    fun receiveCustomerRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as CustomerRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: run {
            println("[customer-service] No replyTo in request: $request")
            return
        }

        println("[customer-service] Received customer request: $request, replyTo=$replyTo, correlationId=$correlationId")

        try {
            if (request.action == "get") {
                val customerId = request.customerId ?: run {
                    val resp = CustomerResponse(
                        requestId = request.requestId,
                        customerId = null,
                        status = "error",
                        payload = null,
                        error = "Missing customerId in request"
                    )
                    messageSender.sendCustomerResponse(resp, replyTo, correlationId)
                    return
                }

                try {
                    val customer: CustomerEntity = customerService.getCustomerById(customerId)
                    val payload: Map<String, Any?> = customer.toMap()

                    val resp = CustomerResponse(
                        requestId = request.requestId,
                        customerId = customer.id,
                        status = "ok",
                        payload = payload as Map<String, Any>,
                        error = null
                    )
                    messageSender.sendCustomerResponse(resp, replyTo, correlationId)

                } catch (nf: Exception) {
                    val resp = CustomerResponse(
                        requestId = request.requestId,
                        customerId = request.customerId,
                        status = "not_found",
                        payload = null,
                        error = nf.message
                    )
                    messageSender.sendCustomerResponse(resp, replyTo, correlationId)
                }
            } else {
                val resp = CustomerResponse(
                    requestId = request.requestId,
                    customerId = request.customerId,
                    status = "error",
                    payload = null,
                    error = "Unsupported action: ${request.action}"
                )
                messageSender.sendCustomerResponse(resp, replyTo, correlationId)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            val resp = CustomerResponse(
                requestId = request.requestId,
                customerId = request.customerId,
                status = "error",
                payload = null,
                error = ex.message
            )
            messageSender.sendCustomerResponse(resp, replyTo, correlationId)
        }
    }
}
