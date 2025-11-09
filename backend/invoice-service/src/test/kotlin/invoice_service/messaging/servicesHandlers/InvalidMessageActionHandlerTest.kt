package invoice_service.messaging.servicesHandlers

import com.uhk.fim.prototype.common.messaging.dto.InvoiceRequest
import com.uhk.fim.prototype.common.messaging.dto.InvoiceResponse
import invoice_service.messaging.MessageSender
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class InvalidMessageActionHandlerTest {

    @Test
    fun `handleInvalidMessageAction sends unsupported response`() {
        val sender: MessageSender = mock()
        val handler = InvalidMessageActionHandler(sender)

        val req = InvoiceRequest(requestId = "r1", invoiceId = 99L, action = "unknown", payload = null)

        handler.handleInvalidMessageAction(req, "corr-1", "reply-qq")

        val captReq = argumentCaptor<InvoiceResponse>()
        verify(sender, times(1)).sendInvoiceResponse(captReq.capture(), eq("reply-qq"), eq("corr-1"))

        val resp = captReq.firstValue
        assert(resp.status.contains("unsupported"))
        assert(resp.error!!.contains("unknown"))
    }
}

