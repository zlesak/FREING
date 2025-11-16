package com.uhk.fim.prototype.common.messaging

import org.aopalliance.intercept.MethodInterceptor
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class RabbitConfig {
    companion object {
        const val EXCHANGE = "freing.exchange"
        const val CUSTOMER_REQUESTS = "customer.requests"
        const val INVOICE_REQUESTS = "invoice.requests"
        const val PAYMENT_REQUESTS = "payment.requests"
        const val RENDERING_REQUESTS = "rendering.requests"
        // responses fronty zde nejsou potřeba
    }

    @Bean
    fun exchange(): DirectExchange = DirectExchange(EXCHANGE)

    @Bean
    fun customerRequestsQueue(): Queue = Queue(CUSTOMER_REQUESTS, true)
    @Bean
    fun invoiceRequestsQueue(): Queue = Queue(INVOICE_REQUESTS, true)
    @Bean
    fun paymentRequestsQueue(): Queue = Queue(PAYMENT_REQUESTS, true)
    @Bean
    fun renderingRequestsQueue(): Queue = Queue(RENDERING_REQUESTS, true)

    @Bean
    fun customerRequestsBinding(exchange: DirectExchange, customerRequestsQueue: Queue): Binding =
        BindingBuilder.bind(customerRequestsQueue).to(exchange).with(CUSTOMER_REQUESTS)
    @Bean
    fun invoiceRequestsBinding(exchange: DirectExchange, invoiceRequestsQueue: Queue): Binding =
        BindingBuilder.bind(invoiceRequestsQueue).to(exchange).with(INVOICE_REQUESTS)
    @Bean
    fun paymentRequestsBinding(exchange: DirectExchange, paymentRequestsQueue: Queue): Binding =
        BindingBuilder.bind(paymentRequestsQueue).to(exchange).with(PAYMENT_REQUESTS)
    @Bean
    fun renderingRequestsBinding(exchange: DirectExchange, renderingRequestsQueue: Queue): Binding =
        BindingBuilder.bind(renderingRequestsQueue).to(exchange).with(RENDERING_REQUESTS)

    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: CachingConnectionFactory, messageConverter: Jackson2JsonMessageConverter): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter
        return template
    }

    @Bean
    @Primary
    fun rabbitListenerContainerFactory(
        connectionFactory: CachingConnectionFactory,
        rabbitListenerAdvice: MethodInterceptor
    ): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setAdviceChain(rabbitListenerAdvice) // global advice
        return factory
    }
}
