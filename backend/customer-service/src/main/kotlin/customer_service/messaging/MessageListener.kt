package customer_service.messaging

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessageListener(private val rabbitTemplate: RabbitTemplate) {
}
