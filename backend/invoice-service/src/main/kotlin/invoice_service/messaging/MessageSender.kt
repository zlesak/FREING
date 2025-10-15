package invoice_service.messaging

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessageSender(private val rabbitTemplate: RabbitTemplate) {
    fun send(message: String) {
        rabbitTemplate.convertAndSend("freing.queue", message)
    }
}

