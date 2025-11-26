package payment_service.services

import com.uhk.fim.prototype.common.messaging.enums.TransactionPayloadType
import com.uhk.fim.prototype.common.security.JwtUserPrincipal
import org.springframework.stereotype.Service
import payment_service.messaging.MessageSender

@Service
class TransactionService(
    private val messageSender: MessageSender
) {

    fun create(invoiceId: Long, customerID: Long){
        val response = messageSender.sendTransactionValidationRequest(invoiceId, payload = mapOf(TransactionPayloadType.CUSTOMER_ID.name to customerID))
        println("response status ${response.status} allow ${response.payload[TransactionPayloadType.ALLOWED_TO_PAY.name]} amount ${response.payload[TransactionPayloadType.AMOUNT.name]}")
    }

    fun capture(token: String){

    }

    fun cancel(){

    }
}