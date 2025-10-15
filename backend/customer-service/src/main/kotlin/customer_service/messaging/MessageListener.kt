package customer_service.messaging

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class MessageListener {
    @RabbitListener(queues = ["freing.queue"])
    fun receive(message: String) {
        println("[customer-service] Received message: $message")
    }
}

