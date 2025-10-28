package customer_service.messaging

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessageSender(private val rabbitTemplate: RabbitTemplate) {
}
