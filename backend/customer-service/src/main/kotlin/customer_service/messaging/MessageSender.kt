package customer_service.messaging

import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.sender.RabbitMessageSender
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessageSender(private val rabbitMessageSender: RabbitMessageSender) {
    fun sendCustomerResponse(response: MessageResponse, replyTo: String, correlationId: String) {
        println("[customer-service] Sending customer response to $replyTo with correlationId=$correlationId")
        rabbitMessageSender.sendResponse(response, replyTo, correlationId)
    }
}
