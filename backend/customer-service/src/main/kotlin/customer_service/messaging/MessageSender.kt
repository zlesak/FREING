package customer_service.messaging

import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessageSender(private val rabbitTemplate: RabbitTemplate) {
    fun sendCustomerResponse(response: CustomerResponse, replyTo: String, correlationId: String) {
        println("[customer-service] Sending customer response to $replyTo with correlationId=$correlationId")
        rabbitTemplate.convertAndSend(
            "",
            replyTo,
            response
        ) { message ->
            message.messageProperties.correlationId = correlationId
            message
        }
    }
}
