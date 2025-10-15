package payment_service.config

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    @Bean
    fun queue(): Queue = Queue("freing.queue", true)

    @Bean
    fun connectionFactory(): CachingConnectionFactory {
        val host = System.getenv("SPRING_RABBITMQ_HOST") ?: "localhost"
        val port = (System.getenv("SPRING_RABBITMQ_PORT") ?: "5672").toInt()
        val user = System.getenv("SPRING_RABBITMQ_USERNAME") ?: "guest"
        val pass = System.getenv("SPRING_RABBITMQ_PASSWORD") ?: "guest"
        val cf = CachingConnectionFactory(host, port)
        cf.username = user
        cf.setPassword(pass)
        return cf
    }

    @Bean
    fun rabbitTemplate(connectionFactory: CachingConnectionFactory): RabbitTemplate {
        return RabbitTemplate(connectionFactory)
    }
}