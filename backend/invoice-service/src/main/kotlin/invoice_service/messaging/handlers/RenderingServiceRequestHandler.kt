package invoice_service.messaging.handlers

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.uhk.fim.prototype.common.config.RabbitConfig
import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.messaging.MessageSender
import com.uhk.fim.prototype.common.messaging.dto.MessageRequest
import com.uhk.fim.prototype.common.messaging.dto.MessageResponse
import com.uhk.fim.prototype.common.messaging.enums.MessageStatus
import com.uhk.fim.prototype.common.messaging.enums.actions.RenderMessageAction
import invoice_service.dtos.reports.requests.InvoiceReportRequest
import invoice_service.dtos.reports.responses.AggregatedReportResponse
import org.springframework.stereotype.Component
import java.util.*

@Component
class RenderingServiceRequestHandler(
    private val messageSender: MessageSender,
) {
    fun renderReportByInvoiceId(
        report: AggregatedReportResponse,
        request: InvoiceReportRequest
    ): ByteArray {
        val response = sendReportRenderRequestAndReturnResponse(report, request)
        if (response.status == MessageStatus.OK) {
            @Suppress("UNCHECKED_CAST")
            val pdfBase64 = (response.payload as? Map<*, *>)?.get("payload") as? String
            return Base64.getDecoder().decode(pdfBase64)
        } else {
            throw NotFoundException("Failed to get pdf data: \\${response.error ?: "Unknown error"}")
        }
    }

    fun sendReportRenderRequestAndReturnResponse(
        report: AggregatedReportResponse,
        request: InvoiceReportRequest
    ): MessageResponse {
        val xmlMapper = XmlMapper()
        xmlMapper.registerModule(JavaTimeModule())
        val reportXml = xmlMapper.writeValueAsString(report)
        val requestXml = xmlMapper.writeValueAsString(request)
        val combinedXml = """
            <ReportWithRequest>
                $reportXml
                $requestXml
            </ReportWithRequest>
            """.trimIndent()
        val messageRequest = MessageRequest(
            route = RabbitConfig.RENDERING_REQUESTS,
            requestId = UUID.randomUUID().toString(),
            action = RenderMessageAction.RENDER_REPORT,
            payload = mapOf(
                "payload" to combinedXml
            )
        )
        return messageSender.sendRequest(messageRequest)
    }
}