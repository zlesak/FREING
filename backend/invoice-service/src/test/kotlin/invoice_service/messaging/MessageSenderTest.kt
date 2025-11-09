package invoice_service.messaging

import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate

class MessageSenderTest {

    @Test
    fun `sendCustomerRequest sets replyTo and correlationId`() {
        val rabbitTemplate: RabbitTemplate = mock()
        // stub execute for queueDeclare during init
        whenever(rabbitTemplate.execute<String>(any())).thenAnswer { null }

        val sender = MessageSender(rabbitTemplate)

        val request = CustomerRequest(requestId = "r1", customerId = 1L, action = "get", payload = null)
        var capturedCallback: ((Message) -> Message)? = null

        whenever(
            rabbitTemplate.convertAndSend(
                eq(RabbitConfig.EXCHANGE),
                eq(RabbitConfig.CUSTOMER_REQUESTS),
                eq(request),
                any<MessagePostProcessor>()
            )
        ).thenAnswer { inv ->
            // the last arg is a MessagePostProcessor
            val processor = inv.getArgument<MessagePostProcessor>(3)
            val msg = Message(ByteArray(0), MessageProperties())
            val processed = processor.postProcessMessage(msg)
            capturedCallback = { m -> processor.postProcessMessage(m) }
            processed
        }

        sender.sendCustomerRequest(request, "corr-123")

        assertNotNull(capturedCallback)
        val msg = Message(ByteArray(0), MessageProperties())
        val processed = capturedCallback!!.invoke(msg)
        assertEquals(sender.replyQueueName, processed.messageProperties.replyTo)
        assertEquals("corr-123", processed.messageProperties.correlationId)
    }

    @Test
    fun `sendInvoiceResponse sets correlationId and sends to replyTo`() {
        val rabbitTemplate: RabbitTemplate = mock()
        whenever(rabbitTemplate.execute<String>(any())).thenAnswer { null }
        val sender = MessageSender(rabbitTemplate)

        val response = InvoiceResponse(
            requestId = "r1",
            invoiceId = 10L,
            status = "ok",
            payload = mapOf("xml" to "x"),
            error = null
        )

        var capturedCallback: ((Message) -> Message)? = null

        whenever(
            rabbitTemplate.convertAndSend(
                eq(""),
                eq("reply-queue"),
                eq(response),
                any<MessagePostProcessor>()
            )
        ).thenAnswer { inv ->
            val processor = inv.getArgument<MessagePostProcessor>(3)
            val msg = Message(ByteArray(0), MessageProperties())
            val processed = processor.postProcessMessage(msg)
            capturedCallback = { m -> processor.postProcessMessage(m) }
            processed
        }

        sender.sendInvoiceResponse(response, "reply-queue", "corr-999")

        assertNotNull(capturedCallback)
        val msg = Message(ByteArray(0), MessageProperties())
        val processed = capturedCallback!!.invoke(msg)
        assertEquals("corr-999", processed.messageProperties.correlationId)
    }

    @Test
    fun `sendInvoiceRequest sets replyTo and correlationId`() {
        val rabbitTemplate: RabbitTemplate = mock()
        whenever(rabbitTemplate.execute<String>(any())).thenAnswer { null }
        val sender = MessageSender(rabbitTemplate)

        val request = InvoiceRequest(requestId = "r2", invoiceId = 5L, action = "renderInvoice", payload = null)

        var capturedCallback: ((Message) -> Message)? = null

        whenever(
            rabbitTemplate.convertAndSend(
                eq(RabbitConfig.EXCHANGE),
                eq(RabbitConfig.INVOICE_REQUESTS),
                eq(request),
                any<MessagePostProcessor>()
            )
        ).thenAnswer { inv ->
            val processor = inv.getArgument<MessagePostProcessor>(3)
            val msg = Message(ByteArray(0), MessageProperties())
            val processed = processor.postProcessMessage(msg)
            capturedCallback = { m -> processor.postProcessMessage(m) }
            processed
        }

        sender.sendInvoiceRequest(request, "corr-55")

        assertNotNull(capturedCallback)
        val msg = Message(ByteArray(0), MessageProperties())
        val processed = capturedCallback!!.invoke(msg)
        assertEquals(sender.replyQueueName, processed.messageProperties.replyTo)
        assertEquals("corr-55", processed.messageProperties.correlationId)
    }
}
