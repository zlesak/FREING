package invoice_service.messaging

import com.uhk.fim.prototype.common.messaging.RabbitConfig
import com.uhk.fim.prototype.common.messaging.dto.CustomerRequest
import com.uhk.fim.prototype.common.messaging.dto.CustomerResponse
import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.services.InvoiceService
import invoice_service.services.ZugferdService
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CompletableFuture

@Component
class MessageListener @Autowired constructor(
    private val messageSender: MessageSender,
    private val messageConverter: MessageConverter,
    private val invoiceService: InvoiceService,
    private val zugferdService: ZugferdService
) {
    private val customerRequests =
        ConcurrentHashMap<String, Triple<InvoiceRequest, Message, invoice_service.models.invoices.Invoice>>()

    private val pendingCustomerResponses =
        ConcurrentHashMap<String, CompletableFuture<CustomerResponse>>()

    fun registerCustomerResponseFuture(correlationId: String, future: CompletableFuture<CustomerResponse>) {
        pendingCustomerResponses[correlationId] = future
    }

    @RabbitListener(queues = [RabbitConfig.INVOICE_REQUESTS])
    fun receiveInvoiceRequest(message: Message) {
        val request = messageConverter.fromMessage(message) as InvoiceRequest
        val correlationId = message.messageProperties.correlationId ?: request.requestId
        val replyTo = message.messageProperties.replyTo ?: return

        println("[invoice-service] Received invoice request: $request, replyTo=$replyTo, correlationId=$correlationId")

        if (request.action == "getById") {
            val invoice = invoiceService.getInvoice(request.invoiceId ?: -1)
            if (invoice == null) {
                val response = InvoiceResponse(
                    requestId = request.requestId,
                    invoiceId = request.invoiceId,
                    status = "not_found",
                    payload = null,
                    error = "Invoice not found"
                )
                messageSender.sendInvoiceResponse(response, replyTo, correlationId)
                return
            }

            val customerId = invoice.customerId
            val customerReq = CustomerRequest(
                requestId = request.requestId,
                customerId = customerId,
                action = "get",
                payload = null
            )

            customerRequests[correlationId] = Triple(request, message, invoice)
            messageSender.sendCustomerRequest(customerReq, correlationId)

        } else {
            val response = InvoiceResponse(
                requestId = request.requestId,
                invoiceId = request.invoiceId,
                status = "unsupported_action",
                payload = null,
                error = "Unsupported action: ${request.action}"
            )
            messageSender.sendInvoiceResponse(response, replyTo, correlationId)
        }
        println("[invoice-service] End $request, replyTo=$replyTo, correlationId=$correlationId")
    }

    @RabbitListener(queues = ["#{messageSender.replyQueueName}"])
    fun receiveCustomerResponse(message: Message) {
        println("[invoice-service] receiveCustomerResponse called, messageProperties: ${message.messageProperties}")
        try {
            val response = messageConverter.fromMessage(message) as CustomerResponse
            val correlationId = message.messageProperties.correlationId ?: response.requestId

            val pending = pendingCustomerResponses.remove(correlationId)
            if (pending != null) {
                println("[invoice-service] Completing pending future for correlationId=$correlationId")
                pending.complete(response)
                return
            }

            val orig = customerRequests.remove(correlationId) ?: run {
                println("[invoice-service] No original invoice request found for correlationId=$correlationId")
                return
            }
            val (origRequest, origMessage, invoice) = orig
            val origReplyTo = origMessage.messageProperties.replyTo ?: run {
                println("[invoice-service] No replyTo found in original message for correlationId=$correlationId")
                return
            }

            println("[invoice-service] Received customer response: $response, correlationId=$correlationId, replyTo=$origReplyTo")

            if (response.status == "ok" && response.payload != null) {
                @Suppress("UNCHECKED_CAST")
                val payload = response.payload as Map<String, Any>

                val xml = try {
                    zugferdService.creteInvoice(invoice, payload)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }

                if (xml != null) {
                    val resp = InvoiceResponse(
                        requestId = origRequest.requestId,
                        invoiceId = origRequest.invoiceId,
                        status = "ok",
                        payload = mapOf("xml" to xml),
                        error = null
                    )
                    messageSender.sendInvoiceResponse(resp, origReplyTo, correlationId)
                } else {
                    val resp = InvoiceResponse(
                        requestId = origRequest.requestId,
                        invoiceId = origRequest.invoiceId,
                        status = "error",
                        payload = null,
                        error = "Failed to generate ZUGFeRD XML"
                    )
                    messageSender.sendInvoiceResponse(resp, origReplyTo, correlationId)
                }
            } else {
                val resp = InvoiceResponse(
                    requestId = response.requestId,
                    invoiceId = response.customerId,
                    status = "error",
                    payload = null,
                    error = response.error ?: "Customer not found"
                )
                messageSender.sendInvoiceResponse(resp, origReplyTo, correlationId)
            }
        } catch (ex: Exception) {
            println("[invoice-service] ERROR in receiveCustomerResponse: ${ex.message}")
            ex.printStackTrace()
        }
    }
}